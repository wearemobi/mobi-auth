// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.domain.MobiUser;
import io.jsonwebtoken.Claims;
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
  private final long refreshExpirationDays;

  public JwtService(
          @Value("${mobi.jwt.secret:SuperSecretMobiKey2026NeedToBeLongEnough32Bytes}") String secret,
          @Value("${mobi.jwt.expiration-hours:24}") long expirationHours,
          @Value("${mobi.jwt.refresh-expiration-days:7}") long refreshExpirationDays) {

    // Franky: Súper llave de cifrado generada con Hashing seguro
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationHours = expirationHours;
    this.refreshExpirationDays = refreshExpirationDays;
  }

  /** Genera el Access Token (La llave de combate diaria) */
  public String generateToken(MobiUser user) {
    var now = Instant.now();
    var expiration = now.plus(expirationHours, ChronoUnit.HOURS);

    var claims = Map.of(
            "roles", user.role(),
            "tenantId", user.tenantId(),
            "orgId", user.orgId(),
            "orgName", user.orgName(),
            "type", "ACCESS" // Robin: Etiqueta de seguridad
    );

    return Jwts.builder()
            .subject(user.email())
            .claims(claims)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
  }

  /** Genera el Refresh Token (La llave maestra de larga duración) */
  public String generateRefreshToken(MobiUser user) {
    var now = Instant.now();
    var expiration = now.plus(refreshExpirationDays, ChronoUnit.DAYS);

    return Jwts.builder()
            .subject(user.email())
            .claim("type", "REFRESH") // Solo necesitamos saber quién es y qué tipo de token es
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact();
  }

  /** Extrae el ADN del Token para validación */
  public Claims extractClaims(String token) {
    return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
  }
}
