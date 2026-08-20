package com.GDP.GDP.service;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.GDP.GDP.dto.user.UserDeleteRequest;
import com.GDP.GDP.dto.user.UserPasswordUpdateRequest;
import com.GDP.GDP.dto.user.UserUpdateRequest;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.EmailAlreadyExistsException;
import com.GDP.GDP.exception.InvalidCredentialsException;
import com.GDP.GDP.repository.UserRepository;
import com.GDP.GDP.service.user.ProfileUpdateResult;
import com.GDP.GDP.service.user.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        currentUser = new User("oldPseudo", "old@mail.com", "hashedPassword", User.Role.ROLE_USER);
        currentUser.setId(userId);
    }

    /* ---------------------------------------------------------
        TESTS GET CURRENT USER
     --------------------------------------------------------- */

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return the profile of the given user, without the password")
        void shouldReturnCurrentUserProfile() {
            var result = userService.getCurrentUser(currentUser);

            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.pseudo()).isEqualTo("oldPseudo");
            assertThat(result.email()).isEqualTo("old@mail.com");
            assertThat(result.role()).isEqualTo(User.Role.ROLE_USER);
        }
    }

    /* ---------------------------------------------------------
        TESTS UPDATE PROFILE
     --------------------------------------------------------- */

    @Nested
    @DisplayName("updateProfile()")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update pseudo without touching email when email unchanged")
        void shouldUpdatePseudoOnly() {
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            UserUpdateRequest request = new UserUpdateRequest("newPseudo", "old@mail.com", "currentPass");

            ProfileUpdateResult result = userService.updateProfile(currentUser, request);

            assertThat(result.user().pseudo()).isEqualTo("newPseudo");
            assertThat(result.user().email()).isEqualTo("old@mail.com");
            assertThat(result.newToken()).isNull();
            verify(userRepository, never()).existsByEmailAndIdNot(any(), any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should update email and reissue a JWT when email changes")
        void shouldUpdateEmailAndReissueToken() {
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            when(userRepository.existsByEmailAndIdNot("new@mail.com", userId)).thenReturn(false);
            when(jwtService.generateToken("new@mail.com")).thenReturn("new-jwt-token");
            UserUpdateRequest request = new UserUpdateRequest("oldPseudo", "new@mail.com", "currentPass");

            ProfileUpdateResult result = userService.updateProfile(currentUser, request);

            assertThat(result.user().email()).isEqualTo("new@mail.com");
            assertThat(result.newToken()).isEqualTo("new-jwt-token");
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when current password is wrong")
        void shouldThrowWhenCurrentPasswordWrong() {
            when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);
            UserUpdateRequest request = new UserUpdateRequest("newPseudo", "old@mail.com", "wrongPass");

            assertThatThrownBy(() -> userService.updateProfile(currentUser, request))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when new email is taken by another user")
        void shouldThrowWhenNewEmailAlreadyTaken() {
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            when(userRepository.existsByEmailAndIdNot("taken@mail.com", userId)).thenReturn(true);
            UserUpdateRequest request = new UserUpdateRequest("oldPseudo", "taken@mail.com", "currentPass");

            assertThatThrownBy(() -> userService.updateProfile(currentUser, request))
                .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should translate a DataIntegrityViolationException from save() into EmailAlreadyExistsException")
        void shouldTranslateDataIntegrityViolation_intoEmailAlreadyExistsException() {
            // Same race as BusinessServiceImpl: the pre-check and the update aren't atomic, so the
            // unique constraint on email is what actually catches a concurrent change to the same address.
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            when(userRepository.existsByEmailAndIdNot("new@mail.com", userId)).thenReturn(false);
            when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
            UserUpdateRequest request = new UserUpdateRequest("oldPseudo", "new@mail.com", "currentPass");

            assertThatThrownBy(() -> userService.updateProfile(currentUser, request))
                .isInstanceOf(EmailAlreadyExistsException.class);
        }
    }

    /* ---------------------------------------------------------
        TESTS UPDATE PASSWORD
     --------------------------------------------------------- */

    @Nested
    @DisplayName("updatePassword()")
    class UpdatePasswordTests {

        @Test
        @DisplayName("Should hash and save the new password when current password matches")
        void shouldUpdatePasswordWhenCurrentPasswordMatches() {
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("currentPass", "newPassword123");

            userService.updatePassword(currentUser, request);

            assertThat(currentUser.getPassword()).isEqualTo("newHashedPassword");
            verify(userRepository).save(currentUser);
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when current password is wrong")
        void shouldThrowWhenCurrentPasswordWrong() {
            when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);
            UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrongPass", "newPassword123");

            assertThatThrownBy(() -> userService.updatePassword(currentUser, request))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository, never()).save(any());
        }
    }

    /* ---------------------------------------------------------
        TESTS DELETE ACCOUNT
     --------------------------------------------------------- */

    @Nested
    @DisplayName("deleteAccount()")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should delete the user when current password matches")
        void shouldDeleteAccountWhenCurrentPasswordMatches() {
            when(passwordEncoder.matches("currentPass", "hashedPassword")).thenReturn(true);
            UserDeleteRequest request = new UserDeleteRequest("currentPass");

            userService.deleteAccount(currentUser, request);

            verify(userRepository).delete(currentUser);
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when current password is wrong, without deleting")
        void shouldThrowWhenCurrentPasswordWrong() {
            when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);
            UserDeleteRequest request = new UserDeleteRequest("wrongPass");

            assertThatThrownBy(() -> userService.deleteAccount(currentUser, request))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(userRepository, never()).delete(any());
        }
    }
}
