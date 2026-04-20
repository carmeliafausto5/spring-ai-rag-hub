package io.github.ragHub.ingestion.service;

import io.github.ragHub.core.port.DocumentQueryPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentQueryService implements DocumentQueryPort {

    private final JdbcTemplate jdbc;

    public DocumentQueryService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<DocumentQueryPort.DocumentSummary> listDocuments() {
        return jdbc.queryForList(
            "SELECT DISTINCT ON (metadata->>'documentId') " +
            "metadata->>'documentId' AS id, " +
            "metadata->>'title' AS title, " +
            "metadata->>'sourceUri' AS sourceUri " +
            "FROM vector_store " +
            "WHERE metadata->>'documentId' IS NOT NULL " +
            "ORDER BY metadata->>'documentId'"
        ).stream()
         .map(row -> new DocumentQueryPort.DocumentSummary(
             (String) row.get("id"),
             (String) row.get("title"),
             (String) row.get("sourceUri"),
             List.of()
         ))
         .toList();
    }

    @Override
    public List<String> listChunkPreviews(String documentId, int limit) {
        return jdbc.queryForList(
            "SELECT content FROM vector_store " +
            "WHERE metadata->>'documentId' = ? " +
            "ORDER BY id LIMIT ?",
            String.class, documentId, limit
        );
    }

    @Override
    public void updateTags(String documentId, List<String> tags) {
        String tagsJson = (tags == null || tags.isEmpty()) ? "[]" :
            tags.stream().map(t -> "\"" + t.replace("\"", "\\\"") + "\"")
                .collect(Collectors.joining(",", "[", "]"));
        jdbc.update(
            "UPDATE vector_store SET metadata = jsonb_set(metadata, '{tags}', ?::jsonb) " +
            "WHERE metadata->>'documentId' = ?",
            tagsJson, documentId
        );
    }
}
