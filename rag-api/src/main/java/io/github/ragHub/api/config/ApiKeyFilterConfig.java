package io.github.ragHub.api.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ApiKeyFilterConfig {

    @Value("${rag.api-key:}")
    private String configuredKey;

    @Bean
    public FilterRegistrationBean<Filter> apiKeyFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                HttpServletResponse res = (HttpServletResponse) response;
                String method = req.getMethod();
                boolean isProtected = ("POST".equals(method) || "DELETE".equals(method));
                if (isProtected) {
                    if (configuredKey.isBlank()) {
                        res.sendError(503, "API key not configured on server");
                        return;
                    }
                    String provided = req.getHeader("X-API-Key");
                    if (!configuredKey.equals(provided)) {
                        res.sendError(401, "Unauthorized");
                        return;
                    }
                }
                chain.doFilter(request, response);
            }
        });
        reg.addUrlPatterns("/api/v1/documents", "/api/v1/documents/*");
        reg.setOrder(1);
        return reg;
    }
}
