package com.GDP.GDP.service.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserResponse;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.EmailAlreadyExistsException;
import com.GDP.GDP.exception.InvalidCredentialsException;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.service.JwtService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private void assertCurrentPasswordMatches(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(User user) {
        return UserResponse.fromEntity(user);
    }

    @Override
    public ProfileUpdateResult updateProfile(User user, UserUpdateRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());

        boolean emailChanged = !user.getEmail().equals(request.email());
        if (emailChanged && userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setPseudo(request.pseudo());
        user.setEmail(request.email());

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Same non-atomic check-then-act race as BusinessServiceImpl.updateBusiness(): the
            // (email) unique constraint is the real guard, this just keeps the loser's error
            // consistent with the non-racy case instead of a generic 409.
            throw new EmailAlreadyExistsException(request.email());
        }

        String newToken = emailChanged ? jwtService.generateToken(user.getEmail()) : null;
        return new ProfileUpdateResult(UserResponse.fromEntity(user), newToken);
    }

    @Override
    public void updatePassword(User user, UserPasswordUpdateRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(User user, UserDeleteRequest request) {
        assertCurrentPasswordMatches(user, request.currentPassword());
        userRepository.delete(user);
    }
}
