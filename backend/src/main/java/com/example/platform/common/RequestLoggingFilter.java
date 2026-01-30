package com.example.platform.common;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * HTTP Request Logging Filter
 * Logs all incoming HTTP requests and responses for debugging and monitoring
 */
@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            
            long startTime = System.currentTimeMillis();
            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            String fullUrl = queryString != null ? uri + "?" + queryString : uri;
            
            // Log incoming request
            logger.info("==> {} {}", method, fullUrl);
            
            try {
                // Continue with the request
                chain.doFilter(request, response);
            } finally {
                // Log response
                long duration = System.currentTimeMillis() - startTime;
                int status = httpResponse.getStatus();
                
                logger.info("<== {} {} - Status: {} - Duration: {}ms", method, fullUrl, status, duration);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
