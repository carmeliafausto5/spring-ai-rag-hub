package io.github.ragHub.core.port;
import java.util.List;
import java.util.Map;

public interface DocumentQueryPort {
    List<Map<String, Object>> listDocuments();
}
