package com.GDP.GDP.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.GDP.GDP.dto.auth.LoginRequest;
import com.GDP.GDP.dto.auth.RegisterRequest;



@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
              
        return ResponseEntity.ok(authService.register(request.getPseudo(), request.getEmail(), request.getPassword()));
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }
    
}
