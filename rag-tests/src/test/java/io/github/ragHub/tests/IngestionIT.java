package io.github.ragHub.tests;

import io.github.ragHub.api.RagHubApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = RagHubApplication.class, webEnvironment = RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class IngestionIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.ai.vectorstore.pgvector.initialize-schema", () -> "true");
        r.add("rag.provider", () -> "openai");
        r.add("rag.api-key", () -> "test");
        r.add("rag.registration-enabled", () -> "true");
    }

    @Autowired
    TestRestTemplate rest;

    String token;

    @BeforeEach
    void auth() {
        Map<String, String> creds = Map.of("username", "testuser", "password", "testpass");
        // register (may already exist on second run — ignore 409)
        rest.postForEntity("/api/v1/auth/register", creds, Map.class);
        ResponseEntity<Map> login = rest.postForEntity("/api/v1/auth/login", creds, Map.class);
        token = (String) login.getBody().get("token");
    }

    @Test
    void ingest_thenListReturnsDocument() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource("hello world".getBytes()) {
            @Override public String getFilename() { return "test.txt"; }
        });
        body.add("title", "IT Test Doc");

        ResponseEntity<Map> upload = rest.exchange(
                "/api/v1/documents/upload", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);
        assertThat(upload.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setBearerAuth(token);
        ResponseEntity<List> list = rest.exchange(
                "/api/v1/documents", HttpMethod.GET,
                new HttpEntity<>(getHeaders), List.class);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody()).isNotEmpty();
        assertThat(list.getBody().toString()).contains("IT Test Doc");
    }
}
