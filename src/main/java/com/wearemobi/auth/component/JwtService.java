// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.domain.MobiUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long expirationHours;

  // Minimalist constructor injection with defaults for local dev
  public JwtService(
      @Value("${mobi.jwt.secret:SuperSecretMobiKey2026NeedToBeLongEnough32Bytes}") String secret,
      @Value("${mobi.jwt.expiration-hours:24}") long expirationHours) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationHours = expirationHours;
  }

  public String generateToken(MobiUser user) {
    // Map.of is modern and immutable
    var claims =
        Map.of(
            "roles", user.role(), // For the MVP, sending as single string
            "tenantId", user.tenantId(),
            "orgId", user.orgId(),
            "orgName", user.orgName());

    var now = Instant.now();
    var expiration = now.plus(expirationHours, ChronoUnit.HOURS);

    return Jwts.builder()
        .subject(user.email())
        .claims(claims)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiration))
        .signWith(secretKey)
        .compact();
  }
}
