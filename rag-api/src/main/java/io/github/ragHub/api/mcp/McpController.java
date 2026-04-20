package io.github.ragHub.api.mcp;

import io.github.ragHub.core.port.DocumentQueryPort;
import io.github.ragHub.core.port.RagQueryPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final RagQueryPort ragQueryPort;
    private final DocumentQueryPort documentQueryPort;

    public McpController(RagQueryPort ragQueryPort, DocumentQueryPort documentQueryPort) {
        this.ragQueryPort = ragQueryPort;
        this.documentQueryPort = documentQueryPort;
    }

    @GetMapping("/tools")
    public ResponseEntity<?> listTools() {
        return ResponseEntity.ok(Map.of("tools", List.of(
            Map.of("name", "query_knowledge_base", "description", "Query the RAG knowledge base"),
            Map.of("name", "list_documents", "description", "List all ingested documents")
        )));
    }

    @PostMapping("/tools/query_knowledge_base")
    public ResponseEntity<?> queryKnowledgeBase(@RequestBody Map<String, String> body) {
        var answer = ragQueryPort.query(body.get("question"), null);
        return ResponseEntity.ok(Map.of("answer", answer.answer(), "sources", answer.sources()));
    }

    @PostMapping("/tools/list_documents")
    public ResponseEntity<?> listDocuments() {
        return ResponseEntity.ok(Map.of("documents", documentQueryPort.listDocuments()));
    }
}
