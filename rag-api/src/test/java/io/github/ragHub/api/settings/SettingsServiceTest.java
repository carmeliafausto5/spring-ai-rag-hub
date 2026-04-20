package io.github.ragHub.api.settings;

import io.github.ragHub.core.port.ProviderSettingsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SettingsServiceTest {

    private JdbcTemplate jdbc;
    private Environment env;
    private SettingsService service;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        env = mock(Environment.class);
        service = new SettingsService(jdbc, env);
    }

    @Test
    void get_returnsDbValueFirst() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq("openai.api-key")))
            .thenReturn(List.of("sk-from-db"));
        assertThat(service.get("openai.api-key")).isEqualTo("sk-from-db");
    }

    @Test
    void get_fallsBackToEnvWhenDbEmpty() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq("openai.api-key")))
            .thenReturn(List.of());
        when(env.getProperty("OPENAI_API_KEY")).thenReturn("sk-from-env");
        assertThat(service.get("openai.api-key")).isEqualTo("sk-from-env");
    }

    @Test
    void get_fallsBackToDefaultWhenBothEmpty() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq("openai.base-url")))
            .thenReturn(List.of());
        when(env.getProperty("OPENAI_BASE_URL")).thenReturn(null);
        assertThat(service.get("openai.base-url")).isEqualTo("https://api.openai.com");
    }

    @Test
    void get_returnsDefaultProviderWhenNothingSet() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq("rag.provider")))
            .thenReturn(List.of());
        when(env.getProperty("RAG_PROVIDER")).thenReturn(null);
        assertThat(service.get("rag.provider")).isEqualTo("openai");
    }

    @Test
    void implementsProviderSettingsPort() {
        assertThat(service).isInstanceOf(ProviderSettingsPort.class);
    }
}
