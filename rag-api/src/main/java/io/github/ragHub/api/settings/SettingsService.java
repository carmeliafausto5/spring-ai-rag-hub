package io.github.ragHub.api.settings;

import io.github.ragHub.core.port.ProviderSettingsPort;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService implements ProviderSettingsPort {

    private static final Map<String, String> DEFAULTS;
    private static final Map<String, String> ENV_MAP = Map.ofEntries(
        Map.entry("rag.provider",       "RAG_PROVIDER"),
        Map.entry("openai.api-key",     "OPENAI_API_KEY"),
        Map.entry("openai.base-url",    "OPENAI_BASE_URL"),
        Map.entry("openai.model",       "OPENAI_MODEL"),
        Map.entry("anthropic.api-key",  "ANTHROPIC_API_KEY"),
        Map.entry("anthropic.base-url", "ANTHROPIC_BASE_URL"),
        Map.entry("anthropic.model",    "ANTHROPIC_MODEL"),
        Map.entry("ollama.base-url",    "OLLAMA_BASE_URL"),
        Map.entry("ollama.model",       "OLLAMA_MODEL")
    );

    static {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("rag.provider",       "openai");
        m.put("openai.api-key",     "");
        m.put("openai.base-url",    "https://api.openai.com");
        m.put("openai.model",       "gpt-4o");
        m.put("anthropic.api-key",  "");
        m.put("anthropic.base-url", "https://api.anthropic.com");
        m.put("anthropic.model",    "claude-sonnet-4-6");
        m.put("ollama.base-url",    "http://localhost:11434");
        m.put("ollama.model",       "llama3.2");
        m.put("rag.rate-limit",     "20");
        m.put("rag.chunk-size",     "512");
        m.put("rag.chunk-overlap",  "64");
        DEFAULTS = Collections.unmodifiableMap(m);
    }

    private final JdbcTemplate jdbc;
    private final Environment env;

    public SettingsService(JdbcTemplate jdbc, Environment env) {
        this.jdbc = jdbc;
        this.env = env;
    }

    @Override
    public String get(String key) {
        List<String> rows = jdbc.queryForList(
            "SELECT value FROM app_settings WHERE key = ?", String.class, key);
        if (!rows.isEmpty() && rows.get(0) != null && !rows.get(0).isBlank()) {
            return rows.get(0);
        }
        String envVar = ENV_MAP.get(key);
        if (envVar != null) {
            String envVal = env.getProperty(envVar);
            if (envVal != null && !envVal.isBlank()) return envVal;
        }
        return DEFAULTS.getOrDefault(key, "");
    }

    public Map<String, String> getAll() {
        Map<String, String> result = new LinkedHashMap<>(DEFAULTS);
        jdbc.query("SELECT key, value FROM app_settings",
            (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                result.put(rs.getString("key"), rs.getString("value")));
        return result;
    }

    public void saveAll(Map<String, String> settings) {
        if (settings.size() > 20)
            throw new IllegalArgumentException("Too many settings in one request");
        for (Map.Entry<String, String> e : settings.entrySet()) {
            if (!DEFAULTS.containsKey(e.getKey())) continue;
            String value = e.getValue();
            if (value != null && value.length() > 1000)
                throw new IllegalArgumentException("Value too long for key: " + e.getKey());
            jdbc.update("""
                INSERT INTO app_settings(key, value, updated_at)
                VALUES (?, ?, NOW())
                ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value, updated_at = NOW()
                """, e.getKey(), value);
        }
    }
}
