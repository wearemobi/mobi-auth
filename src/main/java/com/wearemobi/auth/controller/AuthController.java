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
        log.warn(
            "⚠️ MOBI: Intento de uso de Access Token como Refresh Token por {}",
            claims.getSubject());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Invalid token type"));
      }

      String email = claims.getSubject();

      return userRepository
          .findByEmail(email)
          .map(UserMapper::toDomain)
          .map(
              user -> {
                String newAccessToken = jwtService.generateToken(user);
                log.info("🔥 MOBI: Access Token renovado para tenant: {}", user.tenantId());
                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
              })
          .orElseGet(
              () -> {
                log.error("❌ MOBI: Usuario {} no encontrado en el proceso de refresh.", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
              });

    } catch (Exception e) {
      log.error("❌ MOBI: Fallo en la validación del Refresh Token: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Token expired or invalid"));
    }
  }

  @PostMapping("/token")
  public ResponseEntity<?> getM2MToken(@RequestBody Map<String, String> request) {
    String clientId = request.get("clientId");
    String clientSecret = request.get("clientSecret");

    log.info("🚀 MOBI Trace: Request recibida para clientId: [{}]", clientId);

    // 🔥 Franky: ¡ESTE ES EL LOG QUE NECESITAMOS VER AHORA!
    String generatedHash = passwordEncoder.encode(clientSecret);
    log.info(
        "🧪 MOBI Diagnostic: Para el secreto [{}], el hash que Spring genera es: [{}]",
        clientSecret,
        generatedHash);

    return clientRepository
        .findByClientId(clientId)
        .map(
            client -> {
              log.info("✅ MOBI Trace: Cliente encontrado en DB: {}", client.getAppName());

              boolean matches = passwordEncoder.matches(clientSecret, client.getClientSecretHash());
              log.info("🔑 MOBI Trace: ¿Password coincide?: {}", matches);

              if (!matches) {
                log.warn("❌ MOBI Trace: Password NO coincide para {}", clientId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
              }

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
              log.info("🔥 MOBI Trace: Token M2M generado con éxito");
              return ResponseEntity.ok(Map.of("accessToken", token));
            })
        .orElseGet(
            () -> {
              log.warn("❌ MOBI Trace: Cliente [{}] NO existe en la tabla mobi_client", clientId);
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
  }
}
