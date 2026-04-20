package io.github.ragHub.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Component
public class ApiKeyFilter implements Filter {

    private final byte[] apiKeyBytes;

    public ApiKeyFilter(@Value("${rag.api-key}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("rag.api-key must be set (env: RAG_API_KEY)");
        }
        this.apiKeyBytes = apiKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        if (path.startsWith("/api/v1/documents") && ("POST".equals(method) || "DELETE".equals(method))) {
            String header = req.getHeader("X-API-Key");
            if (header == null || !timingSafeEquals(header)) {
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean timingSafeEquals(String candidate) {
        byte[] candidateBytes = candidate.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(apiKeyBytes, candidateBytes);
    }
}
