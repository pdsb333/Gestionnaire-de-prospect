package com.GDP.GDP.dto.user;

import java.util.UUID;

import com.GDP.GDP.entity.User;

public record UserResponse(
    UUID id,
    String pseudo,
    String email,
    User.Role role
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getPseudo(),
            user.getEmail(),
            user.getRole()
        );
    }
}
