package com.GDP.GDP.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Login and register are unauthenticated (permitAll), so nothing else in the filter chain
// throttles repeated attempts against them; without this, an attacker can brute-force
// passwords or enumerate registered emails (via the 409 on /register) at unlimited speed.
// Keyed on the raw remote address rather than X-Forwarded-For: trusting a client-supplied
// header here would let an attacker reset their own rate-limit key on every request.
@Component
public class AuthRateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of("/api/auth/login", "/api/auth/register");

    @Value("${auth.rate-limit.max-attempts:10}")
    private int maxAttemptsPerWindow;

    @Value("${auth.rate-limit.window-seconds:60}")
    private long windowSeconds;

    private final ConcurrentHashMap<String, RequestWindow> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        RequestWindow window = attemptsByIp.computeIfAbsent(request.getRemoteAddr(), k -> new RequestWindow());
        if (window.registerAttemptAndCheckExceeded(maxAttemptsPerWindow, Duration.ofSeconds(windowSeconds))) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Trop de tentatives, réessayez plus tard.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static final class RequestWindow {
        private long windowStart = System.currentTimeMillis();
        private int count = 0;

        synchronized boolean registerAttemptAndCheckExceeded(int maxAttempts, Duration window) {
            long now = System.currentTimeMillis();
            if (now - windowStart > window.toMillis()) {
                windowStart = now;
                count = 0;
            }
            count++;
            return count > maxAttempts;
        }
    }
}
