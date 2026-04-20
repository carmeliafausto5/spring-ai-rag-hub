package io.github.ragHub.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class RagApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired
    MockMvc mvc;

    @Test
    void settingsGet_returnsDefaults() throws Exception {
        mvc.perform(get("/api/v1/settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['rag.provider']").value("openai"));
    }

    @Test
    void documentsList_returnsEmptyArray() throws Exception {
        mvc.perform(get("/api/v1/documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void settingsPut_withoutApiKey_returns401() throws Exception {
        mvc.perform(put("/api/v1/settings")
                .contentType("application/json")
                .content("{\"rag.provider\":\"ollama\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealth_isUp() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
