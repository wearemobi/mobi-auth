// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.domain.MobiUser;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.mapper.UserMapper;
import com.wearemobi.auth.repository.ClientRepository;
import com.wearemobi.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtService jwtService,
                          UserRepository userRepository,
                          ClientRepository clientRepository,
                          PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token missing"));
        }

        try {
            var claims = jwtService.extractClaims(refreshToken);

            if (!"REFRESH".equals(claims.get("type"))) {
                log.warn("⚠️ MOBI: Intento de uso de Access Token como Refresh Token por {}", claims.getSubject());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid token type"));
            }

            String email = claims.getSubject();

            return userRepository.findByEmail(email)
                    .map(UserMapper::toDomain)
                    .map(user -> {
                        String newAccessToken = jwtService.generateToken(user);
                        log.info("🔥 MOBI: Access Token renovado para tenant: {}", user.tenantId());
                        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
                    })
                    .orElseGet(() -> {
                        log.error("❌ MOBI: Usuario {} no encontrado en el proceso de refresh.", email);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                    });

        } catch (Exception e) {
            log.error("❌ MOBI: Fallo en la validación del Refresh Token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token expired or invalid"));
        }
    }

    @PostMapping("/token")
    public ResponseEntity<?> getM2MToken(@RequestBody Map<String, String> request) {
        String clientId = request.get("clientId");
        String clientSecret = request.get("clientSecret");

        return clientRepository.findByClientId(clientId)
                .filter(client -> passwordEncoder.matches(clientSecret, client.getClientSecretHash()))
                .map(client -> {
                    // Creamos un MobiUser sintético de SISTEMA
                    var systemUser = new MobiUser(
                            client.getId(),
                            client.getAppName() + "@mobi.systems",
                            null,
                            Role.MOBI_SYSTEM_AGENT.name(),
                            client.getTenantId(),
                            client.getOrgId().toString(),
                            client.getAppName()
                    );

                    String token = jwtService.generateToken(systemUser);
                    return ResponseEntity.ok(Map.of(
                            "accessToken", token,
                            "tokenType", "Bearer",
                            "expiresIn", 86400 // 24h
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
