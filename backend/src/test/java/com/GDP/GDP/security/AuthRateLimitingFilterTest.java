package com.GDP.GDP.security;

import java.io.PrintWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthRateLimitingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private AuthRateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AuthRateLimitingFilter();
        ReflectionTestUtils.setField(filter, "maxAttemptsPerWindow", 10);
        ReflectionTestUtils.setField(filter, "windowSeconds", 60L);
    }

    @Test
    @DisplayName("should let requests through on an unlimited path regardless of volume")
    void shouldNotLimitUnrelatedPaths() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/business/get");

        for (int i = 0; i < 20; i++) {
            filter.doFilter(request, response, filterChain);
        }

        verify(filterChain, times(20)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("should let the first 10 login attempts from the same IP through")
    void shouldAllowUpToTheLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.2");

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    @DisplayName("should reject the 11th login attempt within the window with 429")
    void shouldRejectBeyondTheLimit() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.3");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, filterChain);
        }
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response).setStatus(429);
    }

    @Test
    @DisplayName("should track register and login attempts from the same IP against the same budget")
    void shouldShareBudgetAcrossLoginAndRegister() throws Exception {
        when(request.getRemoteAddr()).thenReturn("10.0.0.4");
        when(response.getWriter()).thenReturn(mock(PrintWriter.class));

        when(request.getRequestURI()).thenReturn("/api/auth/login");
        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, response, filterChain);
        }
        when(request.getRequestURI()).thenReturn("/api/auth/register");
        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, response, filterChain);
        }
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(10)).doFilter(request, response);
        verify(response).setStatus(429);
    }

    @Test
    @DisplayName("should track separate IPs independently")
    void shouldTrackIndependentIps() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        when(request.getRemoteAddr()).thenReturn("10.0.0.5");
        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, filterChain);
        }

        when(request.getRemoteAddr()).thenReturn("10.0.0.6");
        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(11)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
}
