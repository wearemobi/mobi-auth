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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class AuthRefreshSystemTest extends BaseSystemTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AuthenticationManager authManager;

  @Test
  @DisplayName("Should exchange Refresh Token for a fresh Access Token")
  void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() throws Exception {
    // ARRANGE: Setup user
    var email = "acme@mobi.com";
    var user = new UserEntity();
    user.setEmail(email);
    user.setOrgId(UUID.randomUUID());
    user.setTenantId("acme");
    user.setOrgName("ACME CORP");
    user.setRoles(Set.of(Role.MOBI_TENANT_OWNER));
    userRepository.save(user);
    userRepository.flush();

    // Generate refresh Token
    var mobiUser = com.wearemobi.auth.mapper.UserMapper.toDomain(user);
    String refreshToken = jwtService.generateRefreshToken(mobiUser);

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
