package com.GDP.GDP.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Size(max = 255) String pseudo,
    @Email @NotBlank @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 255, message = "Password must be at least 8 characters") String password
) {}