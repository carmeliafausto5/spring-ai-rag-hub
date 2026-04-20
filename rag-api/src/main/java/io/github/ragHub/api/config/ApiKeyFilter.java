package io.github.ragHub.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {

    @Value("${rag.api-key:}")
    private String apiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        if (path.startsWith("/api/v1/documents") && ("POST".equals(method) || "DELETE".equals(method))) {
            if (apiKey == null || apiKey.isBlank()) {
                res.setStatus(503);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"API key not configured\"}");
                return;
            }

            String header = req.getHeader("X-API-Key");
            if (header == null || !header.equals(apiKey)) {
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
