package com.GDP.GDP.service.user;

import com.GDP.GDP.dto.user.UserResponse;

public record ProfileUpdateResult(UserResponse user, String newToken) {}
