package io.github.ragHub.api.dto;

import io.github.ragHub.core.domain.SearchMode;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record QueryRequest(
        @NotBlank String question,
        String sessionId,
        List<MessageDto> history,
        SearchMode searchMode
) {
    public record MessageDto(String role, String content) {}
}
