// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.controller;

import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.component.TokenSensor;
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
import org.springframework.security.core.Authentication;
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
  private final TokenSensor tokenSensor;

  public AuthController(
      JwtService jwtService,
      UserRepository userRepository,
      ClientRepository clientRepository,
      PasswordEncoder passwordEncoder,
      TokenSensor tokenSensor) {
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.clientRepository = clientRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenSensor = tokenSensor;
  }

  /**
   * 👤 ENDPOINT: Login para Usuarios (Web/Mobile) Diseñado para funcionar con el curl de parámetros
   * escapeados.
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
    // 🛠️ DEBUG: Franky's Login Bridge Sensor
    tokenSensor.debug("REST Login attempt for user: [{}]", username);

    return userRepository
        .findByEmail(username)
        .map(UserMapper::toDomain)
        .map(this::generateAuthResponse) // 🚀 Uso del Motor Central
        .orElseGet(
            () -> {
              log.warn("Login failed: User [{}] not found in M.O.B.I. database.", username);
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
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
            "Invalid token type. Attempted use of ACCESS token as REFRESH token by subject: {}",
            claims.getSubject());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Invalid token type"));
      }

      String email = claims.getSubject();

      return userRepository
          .findByEmail(email)
          .map(UserMapper::toDomain)
          .map(this::generateAuthResponse) // 🚀 Uso del Motor Central para consistencia
          .orElseGet(
              () -> {
                log.error("User [{}] not found during refresh token validation.", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
              });

    } catch (Exception e) {
      log.error("Refresh token validation failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Token expired or invalid"));
    }
  }

  @PostMapping("/token")
  public ResponseEntity<?> getM2MToken(@RequestBody Map<String, String> request) {
    String clientId = request.get("clientId");
    String clientSecret = request.get("clientSecret");

    // 🛠️ DEBUG: Franky's Periscope Entry
    tokenSensor.debug("Processing M2M token request for clientId: [{}]", clientId);

    return clientRepository
        .findByClientId(clientId)
        .map(
            client -> {
              // 🛠️ DEBUG: Franky's Deep Scan
              tokenSensor.inspectAuth(
                  clientSecret, client.getClientSecretHash(), "M2M Login Attempt");

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

              // 🛠️ DEBUG: Signal of success
              tokenSensor.debug("M2M token successfully generated for clientId: [{}]", clientId);

              return generateAuthResponse(systemUser); // 🚀 Uso del Motor Central
            })
        .orElseGet(
            () -> {
              // 🛠️ DEBUG: Missing target detection
              tokenSensor.debug(
                  "Authentication failed: ClientId [{}] not found in database.", clientId);
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    // 🛡️ FRANKY'S GADGET: Verificación de seguridad antes del abordaje
    if (authentication == null || !authentication.isAuthenticated()) {
      tokenSensor.debug("Identity request failed: No authentication context found.");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    tokenSensor.debug("Identity resolution request for: [{}]", authentication.getName());

    return userRepository
        .findByEmail(authentication.getName())
        .map(UserMapper::toDomain)
        .map(
            mobiUser -> {
              log.info("Identity successfully resolved for tenant: [{}]", mobiUser.tenantId());
              return ResponseEntity.ok(mobiUser);
            })
        .orElseGet(
            () -> {
              log.warn("Identity failed: User [{}] not found in DB", authentication.getName());
              return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            });
  }

  /**
   * ⚙️ MOTOR CENTRAL: Identity Response Engine Centraliza la forja del JWT y el formato del JSON de
   * salida.
   */
  private ResponseEntity<?> generateAuthResponse(MobiUser user) {
    String token = jwtService.generateToken(user);
    log.info("Token successfully issued for: [{}] | Tenant: [{}]", user.email(), user.tenantId());

    return ResponseEntity.ok(
        Map.of(
            "accessToken",
            token,
            "tokenType",
            "Bearer",
            "expiresIn",
            86400, // 24 horas unificado para la v1.7
            "issuedAt",
            System.currentTimeMillis() / 1000));
  }
}
