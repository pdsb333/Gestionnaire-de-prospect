package com.GDP.GDP.controller;


import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.auth.RegisterRequest;
import com.GDP.GDP.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request.pseudo(), request.email(), request.password());
        ResponseCookie cookie = ResponseCookie.from("token", token)
                                .httpOnly(true) //empeche l'accées via js cote cli
                                .secure(false) //bloque envoie pas dns http
                                .sameSite("Lax") //permet requête cross site(microservice)
                                .path("/")  //cookie accessible sur les routes commencant par:
                                .maxAge(86400) //durée de vie du cookie
                                .build();   
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.email(), request.password());
        ResponseCookie cookie = ResponseCookie.from("token", token)
                                .httpOnly(true) //empeche l'accées via js cote cli
                                .secure(false) //bloque envoie pas dns http
                                .sameSite("Lax") //permet requête cross site(microservice)
                                .path("/")  //cookie accessible sur les routes commencant par:
                                .maxAge(86400) //durée de vie du cookie
                                .build();
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("token", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0) 
            .sameSite("None")
            .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.noContent().build();
    }
    
}
