package com.GDP.GDP.controller;

import java.time.Duration; 

import org.springframework.beans.factory.annotation.Value; 
import org.springframework.http.HttpHeaders; 
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.auth.RegisterRequest;
import com.GDP.GDP.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String TOKEN_COOKIE_NAME = "token";

    private final AuthService authService;

    @Value("${app.auth.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${app.auth.cookie.same-site:Lax}")
    private String sameSite;

    // Kept equal to jwt.expiration (see JwtService) so the cookie never outlives the token it
    // carries: a longer-lived cookie would keep resending an already-expired JWT for no benefit.
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {

        String token = authService.register(
                request.pseudo(),
                request.email(),
                request.password()
        );

        return ResponseEntity
                .status(201)
                .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token, Duration.ofMillis(jwtExpirationMs)).toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {

        String token = authService.login(
                request.email(),
                request.password()
        );

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, buildTokenCookie(token, Duration.ofMillis(jwtExpirationMs)).toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, deleteTokenCookie().toString())
                .build();
    }

    private ResponseCookie buildTokenCookie(String token, Duration duration) {

        return ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(duration)
                .build();
    }

    private ResponseCookie deleteTokenCookie() {

        return ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}