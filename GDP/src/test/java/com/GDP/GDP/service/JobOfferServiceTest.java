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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.GDP.GDP.service.joboffer.JobOfferServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobOfferService - Tests unitaires")
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
        UTILITAIRES DE TEST
     --------------------------------------------------------- */

    private JobOfferRequest createJobOfferRequest(String name, String link, Integer relaunchFrequency){
        return new JobOfferRequest(name, link, relaunchFrequency);
    }

    private JobOffer createJobOffer(Long id, String name, String link, Integer relaunchFrequency, Business business) {
        JobOffer jobOffer = new JobOffer(name, link, relaunchFrequency, business);
        jobOffer.setId(id);
        return jobOffer;
    }
    /* ---------------------------------------------------------
        TESTS CREATE JOB OFFER
     --------------------------------------------------------- */ 

@Nested
    @DisplayName("Tests de create()")
    class CreateTests {

        @Test
        @DisplayName("Devrait créer un JobOffer avec des données valides et retourner la response correcte")
        void create_WithValidData_ReturnsJobOfferResponseWithCorrectValues() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com/job", 7);
            JobOffer savedJobOffer = createJobOffer(1L, "Dev Java", "https://example.com/job", 7, testBusiness);

            when(verifyBusinessForUser.verifyBusiness(1L, testUser)).thenReturn(testBusiness);
            when(jobOfferRepository.save(any(JobOffer.class))).thenReturn(savedJobOffer);

            ArgumentCaptor<JobOffer> captor = ArgumentCaptor.forClass(JobOffer.class);

            // When
            JobOfferResponse response = jobOfferService.create(request, 1L, testUser);

            // Then - vérifie l'objet passé à save()
            verify(jobOfferRepository).save(captor.capture());
            JobOffer captured = captor.getValue();

            assertThat(captured.getName()).isEqualTo("Dev Java");
            assertThat(captured.getLink()).isEqualTo("https://example.com/job");
            assertThat(captured.getRelaunchFrequency()).isEqualTo(7);
            assertThat(captured.getBusiness()).isEqualTo(testBusiness);

            // Vérifie la response
            assertThat(response.name()).isEqualTo("Dev Java");
            assertThat(response.link()).isEqualTo("https://example.com/job");
            assertThat(response.relaunchFrequency()).isEqualTo(7);

            // Vérifie les appels de service
            verify(verifyBusinessForUser).verifyBusiness(1L, testUser);
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException quand le business n'appartient pas à l'utilisateur")
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

            // Vérifie que save() n'est jamais appelé
            verify(jobOfferRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait lever ResourceNotFoundException quand le business n'existe pas")
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

            // Vérifie que save() n'est jamais appelé
            verify(jobOfferRepository, never()).save(any());
        }

        @Test
        @DisplayName("Devrait propager l'exception si le repository échoue lors du save()")
        void create_WhenRepositoryFails_PropagatesException() {
            // Given
            JobOfferRequest request = createJobOfferRequest("Dev Java", "https://example.com/job", 7);

            when(verifyBusinessForUser.verifyBusiness(1L, testUser)).thenReturn(testBusiness);
            when(jobOfferRepository.save(any(JobOffer.class))).thenThrow(new RuntimeException("Erreur base de données"));

            // When & Then
            assertThatThrownBy(() -> jobOfferService.create(request, 1L, testUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erreur base de données");
        }
    }
  
}