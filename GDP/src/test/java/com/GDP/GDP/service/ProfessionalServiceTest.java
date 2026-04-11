package com.GDP.GDP.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.professional.ProfessionalRequest;
import com.GDP.GDP.dto.professional.ProfessionalResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.Professional;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.ProfessionalRepository;
import com.GDP.GDP.service.professional.ProfessionalServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProfessionalService Unit Tests")
class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private VerifyBusinessForUser verifyBusinessForUser;

    @InjectMocks
    private ProfessionalServiceImpl service;

    private User user;
    private Business business;
    private ProfessionalRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        business = new Business();
        business.setId(10L);
        business.setUser(user);

        request = new ProfessionalRequest("Doe",
                                          "John",
                                          "Dev", 
                                          "john@doe.com");

    }

    // =========================
    // CREATE
    // =========================

    @Nested
    @DisplayName("Create Professional")
    class CreateProfessionalTests {

        @Test
        @DisplayName("Should create professional successfully")
        void shouldCreateProfessional() {
            when(verifyBusinessForUser.verifyBusiness(10L, user)).thenReturn(business);
            when(professionalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Professional> captor = ArgumentCaptor.forClass(Professional.class);

            ProfessionalResponse response = service.create(request, 10L, user);

            verify(professionalRepository).save(captor.capture());
            Professional saved = captor.getValue();

            assertThat(saved.getFirstName()).isEqualTo("John");
            assertThat(saved.getLastName()).isEqualTo("Doe");
            assertThat(saved.getJob()).isEqualTo("Dev");
            assertThat(saved.getContact()).isEqualTo("john@doe.com");
            assertThat(saved.getBusiness()).isEqualTo(business);
            assertThat(response).isNotNull();

            verifyNoMoreInteractions(professionalRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business not found")
        void shouldThrowWhenBusinessNotFound() {
            when(verifyBusinessForUser.verifyBusiness(10L, user))
                    .thenThrow(new ResourceNotFoundException("Business", 10L));

            assertThatThrownBy(() -> service.create(request, 10L, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(professionalRepository, never()).save(any());
        }
    }


    // =========================
    // UPDATE
    // =========================

    @Nested
    @DisplayName("Update Professional")
    class UpdateProfessionalTests {

        @Test
        @DisplayName("Should update professional fields successfully")
        void shouldUpdateProfessional() {
            // GIVEN
            Professional existing = new Professional();
            existing.setFirstName("Old");

            when(professionalRepository.findByIdAndBusiness_UserId(1L, user.getId()))
                    .thenReturn(Optional.of(existing));

            // WHEN
            ProfessionalResponse response = service.updateProfessional(request, 1L, user);

            // THEN

            verify(professionalRepository).findByIdAndBusiness_UserId(1L, user.getId());

            assertThat(existing.getFirstName()).isEqualTo("John");
            assertThat(existing.getLastName()).isEqualTo("Doe");
            assertThat(existing.getJob()).isEqualTo("Dev");
            assertThat(existing.getContact()).isEqualTo("john@doe.com");
            verify(professionalRepository, never()).save(any());
            assertThat(response).isNotNull();
            verifyNoMoreInteractions(professionalRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when professional not found")
        void shouldThrowWhenProfessionalNotFound() {
            when(professionalRepository.findByIdAndBusiness_UserId(1L, user.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateProfessional(request, 1L, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(professionalRepository, never()).save(any());
        }

    }


    // =========================
    // DELETE
    // =========================

    @Nested
    @DisplayName("Delete Professional")
    class DeleteProfessionalTests {

        @Test
        @DisplayName("Should delete professional successfully")
        void shouldDeleteProfessional() {
            Professional existing = new Professional();

            when(professionalRepository.findByIdAndBusiness_UserId(1L, user.getId()))
                    .thenReturn(Optional.of(existing));

            service.deleteProfessional(1L, user);

            verify(professionalRepository).delete(existing);
            verifyNoMoreInteractions(professionalRepository);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when professional not found")
        void shouldThrowWhenDeletingNotFoundProfessional() {
            when(professionalRepository.findByIdAndBusiness_UserId(1L, user.getId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deleteProfessional(1L, user))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(professionalRepository, never()).delete(any());
        }
    }
}