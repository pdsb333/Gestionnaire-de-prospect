package com.GDP.GDP.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.EmailAlreadyExistsException;
import com.GDP.GDP.exception.InvalidCredentialsException;
import com.GDP.GDP.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthService authService;

    /* ---------------------------------------------------------
        TESTS REGISTER
    --------------------------------------------------------- */

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should throw EmailAlreadyExistsException when email already exists, without saving")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register("pseudo", "taken@test.com", "password123"))
                .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should hash the password before saving")
        void shouldHashPasswordBeforeSaving() {
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
            when(jwtService.generateToken("user@test.com")).thenReturn("token");

            authService.register("pseudo", "user@test.com", "plainPassword");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("hashedPassword");
        }

        @Test
        @DisplayName("should save the new user with ROLE_USER")
        void shouldSaveWithRoleUser() {
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(jwtService.generateToken(any())).thenReturn("token");

            authService.register("pseudo", "user@test.com", "plainPassword");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(User.Role.ROLE_USER);
            assertThat(captor.getValue().getPseudo()).isEqualTo("pseudo");
            assertThat(captor.getValue().getEmail()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should return the JWT generated for the new user's email")
        void shouldReturnGeneratedToken() {
            when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
            when(jwtService.generateToken("user@test.com")).thenReturn("jwt-token-value");

            String token = authService.register("pseudo", "user@test.com", "plainPassword");

            assertThat(token).isEqualTo("jwt-token-value");
        }
    }

    /* ---------------------------------------------------------
        TESTS LOGIN
    --------------------------------------------------------- */

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should authenticate with the given email and password")
        void shouldAuthenticateWithGivenCredentials() {
            User user = new User("pseudo", "user@test.com", "hashedPassword", User.Role.ROLE_USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken("user@test.com")).thenReturn("token");

            authService.login("user@test.com", "plainPassword");

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authManager).authenticate(captor.capture());
            assertThat(captor.getValue().getPrincipal()).isEqualTo("user@test.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("plainPassword");
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when authentication fails (bad password)")
        void shouldThrowWhenBadCredentials() {
            when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

            assertThatThrownBy(() -> authService.login("user@test.com", "wrongPassword"))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("should throw InvalidCredentialsException when the authenticated user cannot be found by email")
        void shouldThrowWhenUserNotFoundAfterAuthentication() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login("user@test.com", "plainPassword"))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("should return the JWT generated for the authenticated user's email")
        void shouldReturnGeneratedToken() {
            User user = new User("pseudo", "user@test.com", "hashedPassword", User.Role.ROLE_USER);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
            when(jwtService.generateToken("user@test.com")).thenReturn("jwt-token-value");

            String token = authService.login("user@test.com", "plainPassword");

            assertThat(token).isEqualTo("jwt-token-value");
        }
    }
}
