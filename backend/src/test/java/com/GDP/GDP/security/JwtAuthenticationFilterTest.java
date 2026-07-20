package com.GDP.GDP.security;

import com.GDP.GDP.entity.User;
import com.GDP.GDP.service.CustomUserDetailsService;
import com.GDP.GDP.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void withTokenCookie(String token) {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("token", token)});
    }

    @Test
    @DisplayName("should authenticate when the token is valid and the user exists")
    void shouldAuthenticate_whenTokenValidAndUserExists() throws Exception {
        withTokenCookie("valid-token");
        User user = new User("pseudo", "user@test.com", "hash", User.Role.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUsername("valid-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should continue unauthenticated when the token is expired or malformed")
    void shouldContinueUnauthenticated_whenTokenInvalid() throws Exception {
        withTokenCookie("bad-token");
        when(jwtService.extractUsername("bad-token")).thenThrow(new RuntimeException("malformed"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should continue unauthenticated, not throw, when the token's user no longer exists")
    void shouldContinueUnauthenticated_whenUserNoLongerExists() throws Exception {
        withTokenCookie("valid-token-deleted-user");
        when(jwtService.extractUsername("valid-token-deleted-user")).thenReturn("ghost@test.com");
        when(userDetailsService.loadUserByUsername("ghost@test.com"))
            .thenThrow(new UsernameNotFoundException("Email not found"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should continue unauthenticated when no token is present")
    void shouldContinueUnauthenticated_whenNoToken() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }
}
