package io.github.ragHub.api.config;

import io.github.ragHub.core.exception.RagException;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RagException.class)
    public ProblemDetail handleRagException(RagException ex) {
        var problem = ProblemDetail.forStatus(500);
        problem.setTitle("RAG Processing Error");
        problem.setDetail(ex.getMessage());
        problem.setType(URI.create("https://github.com/your-org/spring-ai-rag-hub/errors/rag-error"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        var problem = ProblemDetail.forStatus(400);
        problem.setTitle("Invalid Request");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(400);
        problem.setTitle("Validation Failed");
        problem.setDetail(ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", ")));
        return problem;
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        var problem = ProblemDetail.forStatus(413);
        problem.setTitle("File Too Large");
        problem.setDetail("Upload exceeds the maximum allowed size.");
        return problem;
    }
}
