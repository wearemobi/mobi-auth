// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wearemobi.auth.component.JwtService;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.entity.UserEntity;
import com.wearemobi.auth.repository.UserRepository;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "MOBI_OCI_SCIM_URL=http://dummy",
      "MOBI_OCI_TOKEN_URI=http://dummy",
      "MOBI_OCI_ISSUER_URI=http://dummy",
      "MOBI_AUTH_CLIENT_ID=dummy",
      "MOBI_AUTH_CLIENT_SECRET=dummy",
      "MOBI_M2M_CLIENT_ID=dummy",
      "MOBI_M2M_CLIENT_SECRET=dummy",
      "MOBI_JWT_SECRET=SuperSecretMobiKey2026NeedToBeLongEnough32Bytes",
      "MOBI_JWT_EXPIRATION_HOURS=24",
      "MOBI_JWT_REFRESH-EXPIRATION-DAYS=7"
    })
@AutoConfigureMockMvc
class AuthRefreshSystemTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Check 3: Should exchange Refresh Token for a fresh Access Token")
  void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() throws Exception {
    // 1. ARRANGE: Creamos un usuario real en H2
    var email = "logan@mobi.com";
    var user = new UserEntity();
    user.setEmail(email);
    user.setOrgId(UUID.randomUUID());
    user.setTenantId("logan");
    user.setOrgName("LOGAN CARNICERIA");
    user.setRoles(Set.of(Role.MOBI_TENANT_OWNER));
    userRepository.save(user);

    // Generamos el Refresh Token usando el servicio
    var mobiUser = com.wearemobi.auth.mapper.UserMapper.toDomain(user);
    String refreshToken = jwtService.generateRefreshToken(mobiUser);

    // 2. ACT: Golpeamos el endpoint /api/v1/auth/refresh
    var requestBody = Map.of("refreshToken", refreshToken);

    mockMvc
        .perform(
            post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))

        // 3. ASSERT: Verificamos el éxito
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(
            result -> {
              String body = result.getResponse().getContentAsString();
            });
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper().registerModule(new JavaTimeModule());
    }
  }
}
