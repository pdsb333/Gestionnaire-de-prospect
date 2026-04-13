package com.GDP.components;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.BusinessRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerifyBusinessForUser Unit Tests")
class VerifyBusinessForUserTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private VerifyBusinessForUser verifyBusinessForUser;

    private User user;
    private Business business;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        business = new Business();
        business.setId(10L);
        business.setUser(user);
    }

    @Nested
    @DisplayName("verifyBusiness component")
    class verifyBusinessTests {

        @Test
        @DisplayName("Should return business when found for user")
        void shouldReturnBusinessWhenFound() {
            when(businessRepository.findByIdAndUserId(10L, user.getId()))
                    .thenReturn(Optional.of(business));

            Business result = verifyBusinessForUser.verifyBusiness(10L, user);

            assertThat(result).isEqualTo(business);
            verify(businessRepository).findByIdAndUserId(10L, user.getId());
            verifyNoMoreInteractions(businessRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business not found")
        void shouldThrowWhenBusinessNotFound() {
            when(businessRepository.findByIdAndUserId(10L, user.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> verifyBusinessForUser.verifyBusiness(10L, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(businessRepository).findByIdAndUserId(10L, user.getId());
            verifyNoMoreInteractions(businessRepository);
        }

        @Test
        @DisplayName("Should not return business belonging to another user")
        void shouldNotReturnBusinessOfAnotherUser() {
            User otherUser = new User();
            otherUser.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

            when(businessRepository.findByIdAndUserId(10L, otherUser.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> verifyBusinessForUser.verifyBusiness(10L, otherUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(businessRepository).findByIdAndUserId(10L, otherUser.getId());
        }
    }
}
