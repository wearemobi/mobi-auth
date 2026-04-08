// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.domain.MobiUser;
import com.wearemobi.auth.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expirationHours;
  private final long refreshExpirationDays;

  public JwtService(
          @Value("${mobi.jwt.secret:SuperSecretMobiKey2026NeedToBeLongEnough32Bytes}") String secret,
          @Value("${mobi.jwt.expiration-hours:24}") long expirationHours,
          @Value("${mobi.jwt.refresh-expiration-days:7}") long refreshExpirationDays) {

    // Secure HMAC key generation using the provided secret
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationHours = expirationHours;
    this.refreshExpirationDays = refreshExpirationDays;
  }

  /**
   * Generates the Access Token (Daily Combat Key)
   * Includes granular roles and tenant identification for RBAC.
   */
  public String generateToken(MobiUser user) {
    var now = Instant.now();
    var expiration = now.plus(expirationHours, ChronoUnit.HOURS);

    // [FRANKY-DEBUG]: Injecting DNA claims into Access Token for: {}
    var claims =
            Map.of(
                    "roles", List.of(Role.valueOf(user.role()).getAuthority()), // 🚀 Mapping to ROLE_ format
                    "tenantId", user.tenantId(),
                    "orgId", user.orgId(),
                    "orgName", user.orgName(),
                    "type", "ACCESS" // Security tag for token differentiation
            );

    return Jwts.builder()
            .subject(user.email())
            .claims(claims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
  }

  /**
   * Generates the Refresh Token (Long-term Master Key)
   * Minimal claims to ensure security and identity persistence.
   */
  public String generateRefreshToken(MobiUser user) {
    var now = Instant.now();
    var expiration = now.plus(refreshExpirationDays, ChronoUnit.DAYS);

    return Jwts.builder()
            .subject(user.email())
            .claim("type", "REFRESH")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
  }

  /**
   * Extracts all claims from the provided Token
   * Performs signature verification against the internal SecretKey.
   */
  public Claims extractClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
