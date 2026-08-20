package com.GDP.GDP.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class TokenCookieFactory {

    private static final String TOKEN_COOKIE_NAME = "token";

    @Value("${app.auth.cookie.secure:true}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site:None}")
    private String sameSite;

    // Kept equal to jwt.expiration (see JwtService) so the cookie never outlives the token it
    // carries: a longer-lived cookie would keep resending an already-expired JWT for no benefit.
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public ResponseCookie buildTokenCookie(String token) {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpirationMs))
                .build();
    }

    public ResponseCookie deleteTokenCookie() {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
