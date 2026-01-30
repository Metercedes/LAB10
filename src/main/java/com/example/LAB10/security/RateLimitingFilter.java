package com.example.LAB10.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate.limit.requests-per-minute:60}")
    private int maxRequestsPerMinute;

    @Value("${rate.limit.auth-requests-per-minute:10}")
    private int authRequestsPerMinute;

    @Value("${rate.limit.enabled:true}")
    private boolean rateLimitEnabled;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    private static final String[] STRICT_RATE_LIMIT_PATHS = {
            "/api/auth/login",
            "/api/auth/register"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String requestPath = request.getRequestURI();

        int limit = isStrictRateLimitPath(requestPath) ? authRequestsPerMinute : maxRequestsPerMinute;

        if (isRateLimited(clientIp, limit)) {
            log.warn("SECURITY: Rate limit exceeded | IP: {} | URI: {} | Limit: {}/min",
                    clientIp, requestPath, limit);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIp, int maxRequests) {
        long currentMinute = System.currentTimeMillis() / 60000;

        buckets.compute(clientIp, (ip, bucket) -> {
            if (bucket == null || bucket.minute != currentMinute) {
                return new RateLimitBucket(currentMinute, new AtomicInteger(1));
            }
            bucket.count.incrementAndGet();
            return bucket;
        });

        RateLimitBucket bucket = buckets.get(clientIp);
        return bucket != null && bucket.count.get() > maxRequests;
    }

    private boolean isStrictRateLimitPath(String path) {
        for (String strictPath : STRICT_RATE_LIMIT_PATHS) {
            if (path.startsWith(strictPath)) {
                return true;
            }
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitBucket {
        final long minute;
        final AtomicInteger count;

        RateLimitBucket(long minute, AtomicInteger count) {
            this.minute = minute;
            this.count = count;
        }
    }

    public void cleanupOldBuckets() {
        long currentMinute = System.currentTimeMillis() / 60000;
        buckets.entrySet().removeIf(entry -> entry.getValue().minute < currentMinute - 1);
    }
}
