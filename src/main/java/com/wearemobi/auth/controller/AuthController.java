// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.domain.MobiUser;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.mapper.UserMapper;
import com.wearemobi.auth.repository.ClientRepository;
import com.wearemobi.auth.repository.UserRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            JwtService jwtService,
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
                log.warn("Invalid token type. Attempted use of ACCESS token as REFRESH token by subject: {}", claims.getSubject());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid token type"));
            }

            String email = claims.getSubject();

            return userRepository
                    .findByEmail(email)
                    .map(UserMapper::toDomain)
                    .map(
                            user -> {
                                String newAccessToken = jwtService.generateToken(user);
                                log.debug("Access token successfully refreshed for tenant: {}", user.tenantId());
                                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
                            })
                    .orElseGet(
                            () -> {
                                log.error("User [{}] not found during refresh token validation.", email);
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                            });

        } catch (Exception e) {
            log.error("Refresh token validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token expired or invalid"));
        }
    }

    @PostMapping("/token")
    public ResponseEntity<?> getM2MToken(@RequestBody Map<String, String> request) {
        String clientId = request.get("clientId");
        String clientSecret = request.get("clientSecret");

        log.debug("Processing M2M token request for clientId: [{}]", clientId);

        return clientRepository
                .findByClientId(clientId)
                .map(
                        client -> {
                            boolean matches = passwordEncoder.matches(clientSecret, client.getClientSecretHash());

                            if (!matches) {
                                log.warn("Authentication failed: Invalid credentials for clientId [{}]", clientId);
                                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                            }

                            // Creación de usuario de sistema sintético para el token M2M
                            var systemUser =
                                    new MobiUser(
                                            client.getId(),
                                            client.getAppName() + "@mobi.systems",
                                            null,
                                            Role.MOBI_SYSTEM_AGENT.name(),
                                            client.getTenantId(),
                                            client.getOrgId().toString(),
                                            client.getAppName());

                            String token = jwtService.generateToken(systemUser);
                            log.info("M2M token successfully generated for clientId: [{}]", clientId);

                            return ResponseEntity.ok(Map.of(
                                    "accessToken", token,
                                    "tokenType", "Bearer",
                                    "expiresIn", 86400
                            ));
                        })
                .orElseGet(
                        () -> {
                            log.warn("Authentication failed: ClientId [{}] not found in database.", clientId);
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                        });
    }
}