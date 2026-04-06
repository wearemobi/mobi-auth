package com.wearemobi.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wearemobi.auth.entity.ClientEntity;
import com.wearemobi.auth.repository.ClientRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
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
class AuthM2MSystemTest {

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
        agent.setClientSecretHash(passwordEncoder.encode(rawSecret)); // <--- El secreto se guarda hasheado
        agent.setOrgId(UUID.randomUUID());
        agent.setTenantId("logan");
        agent.setAppName("AGENTIC_SUPPORT");
        clientRepository.save(agent);

        // 2. ACT: El agente pide su token
        var requestBody = Map.of(
                "clientId", clientId,
                "clientSecret", rawSecret
        );

        mockMvc.perform(post("/api/v1/auth/token")
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