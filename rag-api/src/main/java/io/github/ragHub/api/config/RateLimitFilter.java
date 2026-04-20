package io.github.ragHub.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitFilter {

    @Value("${rag.rate-limit.requests-per-minute:20}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, long[]> buckets = new ConcurrentHashMap<>();

    @Bean
    public FilterRegistrationBean<Filter> rateLimitFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((request, response, chain) -> {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            String ip = req.getRemoteAddr();
            long now = System.currentTimeMillis();
            if (buckets.size() > 10_000) {
                long cutoff = now - 60_000L;
                buckets.entrySet().removeIf(e -> e.getValue()[0] < cutoff);
            }
            long[] bucket = buckets.computeIfAbsent(ip, k -> new long[]{now, 0});
            synchronized (bucket) {
                if (now - bucket[0] > 60_000L) {
                    bucket[0] = now;
                    bucket[1] = 0;
                }
                if (bucket[1] >= requestsPerMinute) {
                    res.setContentType("application/json");
                    res.setStatus(429);
                    res.getWriter().write("{\"error\":\"Too Many Requests\"}");
                    return;
                }
                bucket[1]++;
            }
            chain.doFilter(request, response);
        });
        reg.addUrlPatterns("/api/v1/rag/*");
        reg.setOrder(2);
        return reg;
    }
}
