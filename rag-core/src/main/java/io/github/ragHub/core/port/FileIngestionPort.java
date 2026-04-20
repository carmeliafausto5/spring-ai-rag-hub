package io.github.ragHub.core.port;

import org.springframework.core.io.Resource;
import java.util.Map;

public interface FileIngestionPort {
    void ingestFile(Resource resource, String title, Map<String, Object> metadata);
}
