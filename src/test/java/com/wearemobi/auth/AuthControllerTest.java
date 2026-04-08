// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.entity.ClientEntity;
import com.wearemobi.auth.repository.ClientRepository;
import com.wearemobi.auth.repository.UserRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class AuthControllerTest extends BaseSystemTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ClientRepository clientRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;

  @MockitoBean private AuthenticationManager authManager;

  @Test
  @DisplayName("Should block AI Agent from accessing Human Identity endpoint (403)")
  void whenAgentTriesToAccessMe_thenReceive403() throws Exception {
    // 1. ARRANGE: Forjamos un agente real en la DB de pruebas
    var clientId = "test-agent-403";
    var rawSecret = "agent-secret-key";

    var agent = new ClientEntity();
    agent.setClientId(clientId);
    agent.setClientSecretHash(passwordEncoder.encode(rawSecret));
    agent.setOrgId(UUID.randomUUID());
    agent.setTenantId("mobi-system");
    agent.setAppName("SECURITY_VALIDATOR");
    clientRepository.save(agent);

    // 2. ACT: El agente obtiene su token (M2M Flow)
    var loginRequest = Map.of("clientId", clientId, "clientSecret", rawSecret);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();

    // Extraemos el token del JSON de respuesta
    String responseJson = result.getResponse().getContentAsString();
    String agentToken = objectMapper.readTree(responseJson).get("accessToken").asText();

    // 3. ASSERT: El agente intenta entrar a /me y el escudo de RBAC lo detiene
    // [FRANKY-DEBUG]: Launching unauthorized boarding attempt with Agent DNA...
    mockMvc
        .perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + agentToken))
        .andExpect(status().isForbidden()); // 🛡️ ¡EL GLORIOSO 403!
  }

  @Test
  @DisplayName("Should allow Human Owner to access Identity endpoint (200)")
  void whenLoginWithCorrectCredentials_thenReceiveToken() throws Exception {
    // 1. ARRANGE: Forjamos al Capitán en la DB de pruebas (H2)
    var username = "carlos@wearemobi.com";
    var password = "password123";

    // --- Soldadura de Datos Locales ---
    var user = new com.wearemobi.auth.entity.UserEntity();
    user.setEmail(username);
    user.setTenantId("carlos-tenant");
    user.setOrgName("M.O.B.I. Test Org");
    user.setOrgId(UUID.randomUUID());
    user.setRoles(java.util.Set.of(Role.MOBI_TENANT_OWNER)); // 🛡️ Rango correcto
    userRepository.save(user);

    // --- Mock del Guardia de OCI ---
    var mockAuth =
        new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
    when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mockAuth);

    // 2. ACT & ASSERT
    mockMvc
        .perform(post("/api/v1/auth/login").param("username", username).param("password", password))
        .andExpect(status().isOk()) // 🚀 ¡Ahora sí, 200 OK garantizado!
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public ObjectMapper objectMapper() {
      return new ObjectMapper().registerModule(new JavaTimeModule());
    }
  }
}
