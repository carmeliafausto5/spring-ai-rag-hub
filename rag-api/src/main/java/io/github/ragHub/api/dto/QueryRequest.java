package io.github.ragHub.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record QueryRequest(
        @NotBlank String question,
        String sessionId,
        List<MessageDto> history
) {
    public record MessageDto(String role, String content) {}
}
