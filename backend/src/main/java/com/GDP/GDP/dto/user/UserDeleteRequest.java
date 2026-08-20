package com.GDP.GDP.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserDeleteRequest(
    @NotBlank(message = "Current password is required") String currentPassword
) {}
