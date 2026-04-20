package io.github.ragHub.api.controller;

import io.github.ragHub.api.audit.AuditService;
import io.github.ragHub.api.service.IngestionJobService;
import io.github.ragHub.core.port.DocumentIngestionPort;
import io.github.ragHub.core.port.DocumentQueryPort;
import io.github.ragHub.core.port.FileIngestionPort;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/html", "text/markdown", "text/plain"
    );

    private final FileIngestionPort fileIngestionPort;
    private final DocumentQueryPort documentQueryPort;
    private final DocumentIngestionPort documentIngestionPort;
    private final IngestionJobService ingestionJobService;
    private final Executor ingestionExecutor;
    private final AuditService auditService;

    public DocumentController(FileIngestionPort fileIngestionPort,
                               DocumentQueryPort documentQueryPort,
                               DocumentIngestionPort documentIngestionPort,
                               IngestionJobService ingestionJobService,
                               @Qualifier("ingestionExecutor") Executor ingestionExecutor,
                               AuditService auditService) {
        this.fileIngestionPort = fileIngestionPort;
        this.documentQueryPort = documentQueryPort;
        this.documentIngestionPort = documentIngestionPort;
        this.ingestionJobService = ingestionJobService;
        this.ingestionExecutor = ingestionExecutor;
        this.auditService = auditService;
    }

    @Operation(summary = "Upload a document for ingestion")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) String tags,
            HttpServletRequest request) {

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
        }

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String docTitle = title != null ? title : filename;
        Map<String, Object> meta = new HashMap<>();
        meta.put("filename", filename);
        if (tags != null && !tags.isBlank()) {
            meta.put("tags", tags);
        }

        String jobId = UUID.randomUUID().toString();
        ingestionJobService.submit(jobId);
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        String ip = request.getRemoteAddr();

        var resource = file.getResource();
        CompletableFuture.runAsync(() -> {
            try {
                fileIngestionPort.ingestFile(resource, docTitle, meta);
                ingestionJobService.complete(jobId);
                auditService.log(actor, "UPLOAD", "document", jobId, docTitle, ip);
            } catch (Exception e) {
                ingestionJobService.fail(jobId, e.getMessage());
            }
        }, ingestionExecutor);

        return ResponseEntity.accepted().body(Map.of("jobId", jobId, "status", "PENDING"));
    }

    @Operation(summary = "Get ingestion job status")
    @GetMapping("/upload/status/{jobId}")
    public ResponseEntity<Map<String, String>> jobStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(Map.of("jobId", jobId, "status", ingestionJobService.getStatus(jobId)));
    }

    @Operation(summary = "List all ingested documents")
    @GetMapping
    public ResponseEntity<List<DocumentQueryPort.DocumentSummary>> list() {
        return ResponseEntity.ok(documentQueryPort.listDocuments());
    }

    @Operation(summary = "Delete a document by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
        documentIngestionPort.delete(id);
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.log(actor, "DELETE", "document", id, null, request.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update tags for a document")
    @PatchMapping("/{id}/tags")
    public ResponseEntity<Void> updateTags(
            @PathVariable String id,
            @RequestBody List<String> tags) {
        documentQueryPort.updateTags(id, tags);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List chunk previews for a document")
    @GetMapping("/{id}/chunks")
    public ResponseEntity<List<String>> chunks(
            @PathVariable String id,
            @RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(documentQueryPort.listChunkPreviews(id, Math.min(limit, 10)));
    }
}
