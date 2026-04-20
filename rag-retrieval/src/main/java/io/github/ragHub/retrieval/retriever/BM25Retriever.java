package io.github.ragHub.retrieval.retriever;

import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class BM25Retriever {

    private static final String SQL = """
            SELECT id, content, metadata::text
            FROM vector_store
            WHERE to_tsvector('english', content) @@ plainto_tsquery('english', ?)
            ORDER BY ts_rank(to_tsvector('english', content), plainto_tsquery('english', ?)) DESC
            LIMIT ?
            """;

    private final JdbcTemplate jdbc;

    public BM25Retriever(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Document> retrieve(String question, int topK) {
        return jdbc.query(SQL, (rs, i) -> {
            String id = rs.getString("id");
            String content = rs.getString("content");
            return new Document(id, content, Map.of());
        }, question, question, topK);
    }
}
