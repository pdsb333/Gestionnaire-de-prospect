package com.GDP.GDP.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
    @NotBlank(message = "Current password is required") String currentPassword,
    @NotBlank @Size(min = 8, max = 255, message = "Password must be at least 8 characters") String newPassword
) {}
