package com.GDP.GDP.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank(message = "Pseudo is required") @Size(max = 255) String pseudo,
    @Email @NotBlank(message = "Email is required") @Size(max = 255) String email,
    @NotBlank(message = "Current password is required") String currentPassword
) {}
