package io.github.ragHub.api;

import io.github.ragHub.api.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-that-is-at-least-256-bits-long-for-hmac",
    "rag.registration-enabled=true"
})
class RagApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;

    private String validToken() {
        return jwtUtil.generate("testuser", "USER");
    }

    // --- Public endpoints ---

    @Test
    void documentsList_isPublic() throws Exception {
        mvc.perform(get("/api/v1/documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void actuatorHealth_isPublicAndUp() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    // --- Settings auth ---

    @Test
    void settingsGet_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/settings"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void settingsGet_withValidToken_returnsDefaults() throws Exception {
        mvc.perform(get("/api/v1/settings")
                .header("Authorization", "Bearer " + validToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['rag.provider']").value("openai"));
    }

    @Test
    void settingsPut_withoutToken_returns401() throws Exception {
        mvc.perform(put("/api/v1/settings")
                .contentType("application/json")
                .content("{\"rag.provider\":\"ollama\"}"))
            .andExpect(status().isUnauthorized());
    }

    // --- JWT validation ---

    @Test
    void anyProtectedEndpoint_withInvalidToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/settings")
                .header("Authorization", "Bearer invalid.token.here"))
            .andExpect(status().isUnauthorized());
    }

    // --- Registration gate ---

    @Test
    void register_whenEnabled_returns201() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"newuser\",\"password\":\"password123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void register_withBlankUsername_returns400() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content("{\"username\":\"\",\"password\":\"password123\"}"))
            .andExpect(status().isBadRequest());
    }
}
