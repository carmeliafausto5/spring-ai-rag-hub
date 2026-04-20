package io.github.ragHub.ingestion.service;

import io.github.ragHub.core.port.DocumentQueryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class DocumentQueryService implements DocumentQueryPort {

    private final JdbcTemplate jdbc;

    public DocumentQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Map<String, Object>> listDocuments() {
        return jdbc.queryForList(
            "SELECT DISTINCT ON (metadata->>'documentId') " +
            "metadata->>'documentId' AS id, " +
            "metadata->>'title' AS title, " +
            "metadata->>'sourceUri' AS sourceUri " +
            "FROM vector_store " +
            "WHERE metadata->>'documentId' IS NOT NULL " +
            "ORDER BY metadata->>'documentId'"
        );
    }
}
