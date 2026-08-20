package com.GDP.GDP.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.auth.RegisterRequest;
import com.GDP.GDP.security.TokenCookieFactory;
import com.GDP.GDP.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenCookieFactory tokenCookieFactory;

    public AuthController(AuthService authService, TokenCookieFactory tokenCookieFactory) {
        this.authService = authService;
        this.tokenCookieFactory = tokenCookieFactory;
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
                .header(HttpHeaders.SET_COOKIE, tokenCookieFactory.buildTokenCookie(token).toString())
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
                .header(HttpHeaders.SET_COOKIE, tokenCookieFactory.buildTokenCookie(token).toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, tokenCookieFactory.deleteTokenCookie().toString())
                .build();
    }
}
