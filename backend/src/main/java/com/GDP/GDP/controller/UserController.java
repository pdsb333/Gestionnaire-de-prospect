package com.GDP.GDP.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserResponse;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.security.TokenCookieFactory;
import com.GDP.GDP.service.user.ProfileUpdateResult;
import com.GDP.GDP.service.user.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TokenCookieFactory tokenCookieFactory;

    public UserController(UserService userService, TokenCookieFactory tokenCookieFactory) {
        this.userService = userService;
        this.tokenCookieFactory = tokenCookieFactory;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(userService.getCurrentUser(user.getUser()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        ProfileUpdateResult result = userService.updateProfile(user.getUser(), request);

        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
        if (result.newToken() != null) {
            // Email changed: the JWT subject (email) is now stale, so the old cookie would stop
            // authenticating on the very next request. Reissue it here, same as AuthController.login.
            response.header(HttpHeaders.SET_COOKIE, tokenCookieFactory.buildTokenCookie(result.newToken()).toString());
        }
        return response.body(result.user());
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserPasswordUpdateRequest request
    ) {
        userService.updatePassword(user.getUser(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserDeleteRequest request
    ) {
        userService.deleteAccount(user.getUser(), request);
        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE, tokenCookieFactory.deleteTokenCookie().toString())
                .build();
    }
}
