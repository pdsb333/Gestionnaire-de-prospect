package com.GDP.GDP.service.user;

import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserResponse;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.entity.User;

public interface UserService {
    UserResponse getCurrentUser(User user);
    ProfileUpdateResult updateProfile(User user, UserUpdateRequest request);
    void updatePassword(User user, UserPasswordUpdateRequest request);
    void deleteAccount(User user, UserDeleteRequest request);
}
