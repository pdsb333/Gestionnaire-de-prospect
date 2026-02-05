package com.GDP.GDP.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank String pseudo,
    @Email @NotBlank String email,
    @NotBlank String password
) {}