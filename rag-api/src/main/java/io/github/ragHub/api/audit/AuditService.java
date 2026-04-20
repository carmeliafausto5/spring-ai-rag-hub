package io.github.ragHub.api.audit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Async
    public void log(String actor, String action, String resourceType, String resourceId, String detail, String ipAddress) {
        repo.save(new AuditLog(actor, action, resourceType, resourceId, detail, ipAddress));
    }
}
