// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wearemobi.auth.entity.ClientEntity;
import com.wearemobi.auth.repository.ClientRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

class AuthM2MSystemTest extends BaseSystemTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ClientRepository clientRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Check 4: Should authenticate a System Agent (M2M) via Client Credentials")
  void shouldReturnTokenForValidSystemAgent() throws Exception {
    // 1. ARRANGE: Forjamos un agente en la DB
    var clientId = "agentic-java-001";
    var rawSecret = "super-secret-m2m-key";

    var agent = new ClientEntity();
    agent.setClientId(clientId);
    agent.setClientSecretHash(
        passwordEncoder.encode(rawSecret)); // <--- El secreto se guarda hasheado
    agent.setOrgId(UUID.randomUUID());
    agent.setTenantId("acme");
    agent.setAppName("AGENTIC_SUPPORT");
    clientRepository.save(agent);

    // 2. ACT: El agente pide su token
    var requestBody =
        Map.of(
            "clientId", clientId,
            "clientSecret", rawSecret);

    mockMvc
        .perform(
            post("/api/v1/auth/token")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))

        // 3. ASSERT: Verificamos que se le entregue la llave de sistema
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists())
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper().registerModule(new JavaTimeModule());
    }
  }
}
