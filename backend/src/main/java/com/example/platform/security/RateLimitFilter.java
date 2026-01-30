package com.example.platform.security;

import com.example.platform.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple rate limiting filter for authentication endpoints
 * Uses in-memory sliding window counter per IP address
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final long windowSeconds;
    private final ObjectMapper objectMapper;

    // IP -> (timestamp -> count)
    private final Map<String, Map<Long, AtomicInteger>> requestCounts = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.auth.max-requests}") int maxRequests,
            @Value("${app.rate-limit.auth.window-seconds}") long windowSeconds,
            ObjectMapper objectMapper) {
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Only apply rate limiting to auth endpoints
        if (path.endsWith("/auth/register") || path.endsWith("/auth/login")) {
            String clientIp = getClientIp(request);
            
            if (isRateLimited(clientIp)) {
                sendTooManyRequestsResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIp) {
        long currentWindow = System.currentTimeMillis() / 1000 / windowSeconds;
        
        Map<Long, AtomicInteger> ipRequests = requestCounts.computeIfAbsent(clientIp, k -> new ConcurrentHashMap<>());
        
        // Clean up old windows
        ipRequests.entrySet().removeIf(entry -> entry.getKey() < currentWindow);
        
        AtomicInteger count = ipRequests.computeIfAbsent(currentWindow, k -> new AtomicInteger(0));
        int currentCount = count.incrementAndGet();
        
        return currentCount > maxRequests;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse("TOO_MANY_REQUESTS", 
                "Too many authentication attempts. Please try again later.");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
