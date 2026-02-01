package com.example.platform.security;

import com.example.platform.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter
 * Extracts and validates JWT from Authorization header
 * Sets user principal in request attribute for controllers to use
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Whitelist: public routes that don't require JWT verification
        if (isPublicRoute(path, method)) {
            // For public routes, optionally extract userId if token is present (but don't require it)
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    UUID userId = jwtService.verifyToken(token);
                    request.setAttribute("userId", userId);
                } catch (Exception e) {
                    // For public routes, ignore invalid tokens (don't fail)
                }
            }
            filterChain.doFilter(request, response);
            return;
        }

        // For protected routes, token is required
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        try {
            UUID userId = jwtService.verifyToken(token);
            // Set user ID in request attribute for controllers to access
            request.setAttribute("userId", userId);
        } catch (Exception e) {
            // Invalid token - return 401
            sendUnauthorizedResponse(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the route is public and doesn't require JWT verification
     */
    private boolean isPublicRoute(String path, String method) {
        // POST /auth/register
        if ("POST".equals(method) && path.endsWith("/auth/register")) {
            return true;
        }
        // POST /auth/login
        if ("POST".equals(method) && path.endsWith("/auth/login")) {
            return true;
        }
        // POST /auth/refresh
        if ("POST".equals(method) && path.endsWith("/auth/refresh")) {
            return true;
        }
        // GET /posts
        if ("GET".equals(method) && path.endsWith("/posts")) {
            return true;
        }
        // GET /posts/{postId}/comments (matches pattern like /posts/UUID/comments)
        if ("GET".equals(method) && path.matches(".*/posts/[^/]+/comments")) {
            return true;
        }
        // GET /health
        if ("GET".equals(method) && path.endsWith("/health")) {
            return true;
        }
        return false;
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse("UNAUTHORIZED", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
