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
import org.springframework.security.access.prepost.PreAuthorize;
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
   * 👤 ENDPOINT: Human User Login (Web/Mobile). Designed to handle URL-encoded parameters for broad
   * client compatibility.
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
    // 🛠️ DEBUG: Franky's Login Bridge Sensor
    tokenSensor.debug("REST Login attempt for user: [{}]", username);

    return userRepository
        .findByEmail(username)
        .map(UserMapper::toDomain)
        .map(this::generateAuthResponse) // 🚀 Triggering Core Engine
        .orElseGet(
            () -> {
              log.warn("Login failed: User [{}] not found in M.O.B.I. database.", username);
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
  }

  /** 🔄 ENDPOINT: Token Refresh. Validates the Refresh Token and issues a new Access Token pair. */
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
            "Invalid token type attempt. ACCESS token used as REFRESH by subject: {}",
            claims.getSubject());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Map.of("error", "Invalid token type"));
      }

      String email = claims.getSubject();

      return userRepository
          .findByEmail(email)
          .map(UserMapper::toDomain)
          .map(this::generateAuthResponse) // 🚀 Ensuring consistency via Core Engine
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

  /**
   * 🤖 ENDPOINT: M2M Token Generation. Authenticates internal/external services (AI Agents) using
   * Client Credentials.
   */
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

              // Synthetic System User creation for M2M tokens
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

              return generateAuthResponse(systemUser); // 🚀 Core Engine deployment
            })
        .orElseGet(
            () -> {
              // 🛠️ DEBUG: Missing target detection
              tokenSensor.debug(
                  "Authentication failed: ClientId [{}] not found in database.", clientId);
              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            });
  }

  /**
   * 🛡️ ENDPOINT: Identity Resolution. Restricted to Human Operators. Agents are forbidden from
   * resolving full identity here.
   */
  @GetMapping("/me")
  @PreAuthorize(
      "hasAnyRole('MOBI_TENANT_OWNER', 'MOBI_CORE_ADMIN')") // [FRANKY-DEBUG]: Shielding identity
  // from AI Polizones! AUUU!
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {

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
   * ⚙️ CORE ENGINE: Identity Response Engine. Centralizes JWT generation and output JSON formatting
   * for all authentication flows.
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
            86400, // Unified 24h expiration for v1.8
            "issuedAt",
            System.currentTimeMillis() / 1000));
  }
}
