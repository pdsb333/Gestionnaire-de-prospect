package com.GDP.GDP.service;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.JobOfferRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.GDP.GDP.service.joboffer.JobOfferServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobOfferService - Unit tests")
class JobOfferServiceImplTest {

    @Mock
    private JobOfferRepository jobOfferRepository;

    @Mock
    private VerifyBusinessForUser verifyBusinessForUser;

    @InjectMocks
    private JobOfferServiceImpl jobOfferService;

    private User testUser;
    private Business testBusiness;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());

        testBusiness = new Business();
        testBusiness.setId(1L);
        testBusiness.setUser(testUser);
    }

    /* ---------------------------------------------------------
        TEST UTILITIES
     --------------------------------------------------------- */

    private JobOfferRequest createJobOfferRequest(String name, String link, Integer relaunchFrequency) {
        return new JobOfferRequest(name, link, relaunchFrequency);
    }

    private JobOffer createJobOffer(Long id, String name, String link, Integer relaunchFrequency, Business business) {
        JobOffer jobOffer = new JobOffer(name, link, relaunchFrequency, business);
        jobOffer.setId(id);
        return jobOffer;
    }

    /* ---------------------------------------------------------
        CREATE TESTS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("create() tests")
    class CreateTests {

        @Test
        @DisplayName("Should create a JobOffer with valid data and return the correct response")
        void create_WithValidData_ReturnsJobOfferResponseWithCorrectValues() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com/job", 7);
            JobOffer savedJobOffer = createJobOffer(1L, "Dev Java", "https://example.com/job", 7, testBusiness);

            when(verifyBusinessForUser.verifyBusiness(1L, testUser)).thenReturn(testBusiness);
            when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(savedJobOffer);

            ArgumentCaptor<JobOffer> captor = ArgumentCaptor.forClass(JobOffer.class);

            // When
            JobOfferResponse response = jobOfferService.create(request, 1L, testUser);

            // Then - verify the object passed to save()
            verify(jobOfferRepository).save(captor.capture());
            JobOffer captured = captor.getValue();

            assertThat(captured.getName()).isEqualTo("Dev Java");
            assertThat(captured.getLink()).isEqualTo("https://example.com/job");
            assertThat(captured.getRelaunchFrequency()).isEqualTo(7);
            assertThat(captured.getBusiness()).isEqualTo(testBusiness);

            // Verify the response
            assertThat(response.name()).isEqualTo("Dev Java");
            assertThat(response.link()).isEqualTo("https://example.com/job");
            assertThat(response.relaunchFrequency()).isEqualTo(7);

            // Verify security check was called with correct arguments
            verify(verifyBusinessForUser).verifyBusiness(1L, testUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business does not belong to user")
        void create_WhenBusinessNotOwnedByUser_ThrowsResourceNotFoundException() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com", 7);

            when(verifyBusinessForUser.verifyBusiness(999L, testUser))
                .thenThrow(new ResourceNotFoundException("Business", 999L));

            // When & Then
            assertThatThrownBy(() -> jobOfferService.create(request, 999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business")
                .hasMessageContaining("999");

            verify(jobOfferRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business does not exist")
        void create_WhenBusinessNotFound_ThrowsResourceNotFoundException() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com", 7);

            when(verifyBusinessForUser.verifyBusiness(888L, testUser))
                .thenThrow(new ResourceNotFoundException("Business", 888L));

            // When & Then
            assertThatThrownBy(() -> jobOfferService.create(request, 888L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Business")
                .hasMessageContaining("888");

            verify(jobOfferRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should propagate exception when repository fails on save()")
        void create_WhenRepositoryFails_PropagatesException() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com/job", 7);

            when(verifyBusinessForUser.verifyBusiness(1L, testUser)).thenReturn(testBusiness);
            when(jobOfferRepository.save(any(JobOffer.class))).thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> jobOfferService.create(request, 1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
        }
    }

    /* ---------------------------------------------------------
        UPDATE TESTS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("updateJobOffer() tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update all fields and return the modified JobOffer")
        void update_WithAllFieldsChanged_ReturnsUpdatedJobOffer() {
            // Given
            JobOffer existingJobOffer = createJobOffer(1L, "Old Name", "https://old.com", 5, testBusiness);
            JobOfferRequest updateRequest = createJobOfferRequest("New Name", "https://new.com", 10);

            when(jobOfferRepository.findByIdAndBusiness_UserId(1L, testUser.getId()))
                .thenReturn(Optional.of(existingJobOffer));

            // When
            JobOfferResponse response = jobOfferService.updateJobOffer(updateRequest, 1L, testUser);

            // Then
            assertThat(response.name()).isEqualTo("New Name");
            assertThat(response.link()).isEqualTo("https://new.com");
            assertThat(response.relaunchFrequency()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should update only the name when only the name changes")
        void update_WithOnlyNameChanged_UpdatesOnlyName() {
            // Given
            JobOffer existingJobOffer = createJobOffer(2L, "Old Name", "https://same.com", 7, testBusiness);
            JobOfferRequest updateRequest = createJobOfferRequest("New Name", "https://same.com", 7);

            when(jobOfferRepository.findByIdAndBusiness_UserId(2L, testUser.getId()))
                .thenReturn(Optional.of(existingJobOffer));

            // When
            JobOfferResponse response = jobOfferService.updateJobOffer(updateRequest, 2L, testUser);

            // Then
            assertThat(response.name()).isEqualTo("New Name");
            assertThat(response.link()).isEqualTo("https://same.com");
            assertThat(response.relaunchFrequency()).isEqualTo(7);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when JobOffer does not exist")
        void update_WhenJobOfferNotFound_ThrowsResourceNotFoundException() {
            // Given
            JobOfferRequest updateRequest = createJobOfferRequest("Dev Java", "https://example.com", 7);

            when(jobOfferRepository.findByIdAndBusiness_UserId(999L, testUser.getId()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jobOfferService.updateJobOffer(updateRequest, 999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("JobOffer")
                .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when JobOffer does not belong to user")
        void update_WhenJobOfferNotOwnedByUser_ThrowsResourceNotFoundException() {
            // Given
            JobOfferRequest updateRequest = createJobOfferRequest("Dev Java", "https://example.com", 7);

            when(jobOfferRepository.findByIdAndBusiness_UserId(888L, testUser.getId()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jobOfferService.updateJobOffer(updateRequest, 888L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("JobOffer")
                .hasMessageContaining("888");
        }
    }

    /* ---------------------------------------------------------
        DELETE TESTS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("deleteJobOffer() tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete an existing JobOffer without throwing an exception")
        void delete_WithValidJobOfferId_DeletesSuccessfully() {
            // Given
            JobOffer existingJobOffer = createJobOffer(1L, "Dev to Delete", "https://example.com", 7, testBusiness);

            when(jobOfferRepository.findByIdAndBusiness_UserId(1L, testUser.getId()))
                .thenReturn(Optional.of(existingJobOffer));

            // When
            jobOfferService.deleteJobOffer(1L, testUser);

            // Then - verify delete was actually called with the correct object
            verify(jobOfferRepository).delete(existingJobOffer);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when JobOffer to delete does not exist")
        void delete_WhenJobOfferNotFound_ThrowsResourceNotFoundException() {
            // Given
            when(jobOfferRepository.findByIdAndBusiness_UserId(999L, testUser.getId()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jobOfferService.deleteJobOffer(999L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("JobOffer")
                .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when JobOffer does not belong to user")
        void delete_WhenJobOfferNotOwnedByUser_ThrowsResourceNotFoundException() {
            // Given
            when(jobOfferRepository.findByIdAndBusiness_UserId(888L, testUser.getId()))
                .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> jobOfferService.deleteJobOffer(888L, testUser))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("JobOffer")
                .hasMessageContaining("888");
        }
    }
}