// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.entity.UserEntity;
import com.wearemobi.auth.mapper.UserMapper;
import com.wearemobi.auth.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class TenantFortressTest {

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("Should persist UserEntity and generate a valid JWT with Multi-tenant Claims")
  void shouldPersistUserAndGenerateValidMobiToken() {
    // ---------------------------------------------------------
    // 1. ARRANGE & ACT: Check 1 - JPA Persistence
    // ---------------------------------------------------------
    var orgId = UUID.randomUUID();
    var acme = new UserEntity();
    acme.setEmail("acme@mobi.com");
    acme.setOrgId(orgId);
    acme.setTenantId("acme");
    acme.setOrgName("ACME CORP");
    acme.setRoles(Set.of(Role.MOBI_TENANT_OWNER));

    // Save to H2 in-memory database
    var savedUser = userRepository.save(acme);
    userRepository.flush();

    // Validate that the DB assigned a Primary Key (UUID)
    assertThat(savedUser.getId()).isNotNull();

    // Find by email as OciAuthenticationProvider would do
    var foundUserOpt = userRepository.findByEmail("acme@mobi.com");
    assertThat(foundUserOpt).isPresent();

    // ---------------------------------------------------------
    // 2. ARRANGE & ACT: Check 2 - M.O.B.I. Token Generation
    // ---------------------------------------------------------
    // Instantiate service with a secure test key (>32 bytes)
    String testSecret = "SuperSecretTestKeyForMobiAuth2026!!!";
    var jwtService = new JwtService(testSecret, 1, 1);

    var mobiUser = UserMapper.toDomain(foundUserOpt.get());
    String token = jwtService.generateToken(mobiUser);

    assertThat(token).isNotBlank();

    // ---------------------------------------------------------
    // 3. ASSERT: Decoding and Claims Validation (Acceptance Criteria)
    // ---------------------------------------------------------
    var secretKey = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
    var claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();

    assertThat(claims.getSubject()).isEqualTo("acme@mobi.com");
    assertThat(claims.get("tenantId", String.class)).isEqualTo("acme");

    var roles = (List<String>) claims.get("roles", List.class);
    assertThat(roles).containsExactlyInAnyOrder("ROLE_MOBI_TENANT_OWNER");
  }
}
