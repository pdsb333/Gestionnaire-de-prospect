package com.GDP.GDP.service;

import java.util.List;
import java.util.Optional;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.Professional;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.exception.business.BusinessAlreadyExistsException;
import com.GDP.GDP.repository.BusinessRepository;
import com.GDP.GDP.service.business.BusinessServiceImpl;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @InjectMocks
    private BusinessServiceImpl businessService;

    private User currentUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        currentUser = new User();
        currentUser.setId(userId);
    }

    /* ---------------------------------------------------------
        UTILITAIRES DE TEST
     --------------------------------------------------------- */

    private Business createBusiness(String name, String description, String contact, User user) {
        Business business = new Business(name, description, contact, user);
        business.setId(1L);
        return business;
    }

    private Business withJobOffer(Business business) {
        JobOffer job = new JobOffer("Job A", "LinkA", 2, business);
        business.addJobOffer(job);
        return business;
    }

    private Business withProfessional(Business business) {
        Professional p = new Professional("First", "Last", "Role", "Contact", business);
        business.addProfessional(p);
        return business;
    }

    private BusinessRequest createBusinessRequest(
        String name,
        String description,
        String contact
    ) {
        return new BusinessRequest(name, description, contact);
    }

    /* ---------------------------------------------------------
        TESTS GET BUSINESS BY USER ID
     --------------------------------------------------------- */

    @Nested
    @DisplayName("getBusinessByUserId()")
    class GetBusinessByUserIdTests {

        @Test
        @DisplayName("Should return all businesses for current user")
        void shouldReturnAllBusinessesForCurrentUser() {
            // Arrange
            Business b1 = createBusiness("Entreprise A", "Description A", "Contact A", currentUser);
            Business b2 = createBusiness("Entreprise B", "Description B", "Contact B", currentUser);
            when(businessRepository.findByUser_Id(userId)).thenReturn(List.of(b1, b2));

            // Act
            List<BusinessResponse> result = businessService.getBusinessByUserId(currentUser);

            // Assert
            assertThat(result)
                .hasSize(2)
                .extracting(BusinessResponse::name)
                .containsExactly("Entreprise A", "Entreprise B");
        }

        @Test
        @DisplayName("Should return businesses with job offers and professionals")
        void shouldReturnBusinessesWithRelations() {
            // Arrange
            Business business = createBusiness("Entreprise A", "Description A", "Contact A", currentUser);
            withJobOffer(business);
            withProfessional(business);
            when(businessRepository.findByUser_Id(userId)).thenReturn(List.of(business));

            // Act
            List<BusinessResponse> result = businessService.getBusinessByUserId(currentUser);

            // Assert
            assertThat(result)
                .hasSize(1)
                .first()
                .satisfies(response -> {
                    assertThat(response.name()).isEqualTo("Entreprise A");
                    assertThat(response.jobOffersList()).hasSize(1);
                    assertThat(response.professionalsList()).hasSize(1);
                });
        }

        @Test
        @DisplayName("Should return empty list when user has no businesses")
        void shouldReturnEmptyListWhenUserHasNoBusinesses() {
            // Arrange
            when(businessRepository.findByUser_Id(userId)).thenReturn(List.of());

            // Act
            List<BusinessResponse> result = businessService.getBusinessByUserId(currentUser);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    /* ---------------------------------------------------------
        TESTS CREATE BUSINESS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("create()")
    class CreateBusinessTests {

        @Test
        @DisplayName("Should create business with valid data")
        void shouldCreateBusinessWithValidData() {
            // Arrange
            BusinessRequest request = createBusinessRequest("Entreprise A", "Description A", "Contact A");
            when(businessRepository.existsByNameAndUser_Id("Entreprise A", userId)).thenReturn(false);
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.create(currentUser, request);

            // Assert
            assertThat(result)
                .extracting(
                    BusinessResponse::name,
                    BusinessResponse::description,
                    BusinessResponse::recruitmentServiceContact
                )
                .containsExactly("Entreprise A", "Description A", "Contact A");
        }

        @Test
        @DisplayName("Should trim business name before saving")
        void shouldTrimBusinessNameBeforeSaving() {
            // Arrange
            BusinessRequest request = createBusinessRequest("  Entreprise A  ", "Description", "Contact");
            when(businessRepository.existsByNameAndUser_Id("Entreprise A", userId)).thenReturn(false);
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.create(currentUser, request);

            // Assert
            assertThat(result.name()).isEqualTo("Entreprise A");
        }

        @Test
        @DisplayName("Should fail when trimmed name already exists")
        void shouldFailWhenTrimmedNameAlreadyExists() {
            // Arrange
            BusinessRequest request = createBusinessRequest("  Entreprise A  ", "Desc", "Contact");
            when(businessRepository.existsByNameAndUser_Id("Entreprise A", userId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> businessService.create(currentUser, request))
                .isInstanceOf(BusinessAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw BusinessAlreadyExistsException when exact name exists")
        void shouldThrowExceptionWhenExactNameExists() {
            // Arrange
            BusinessRequest request = createBusinessRequest("Entreprise A", "Description", "Contact");
            when(businessRepository.existsByNameAndUser_Id("Entreprise A", userId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> businessService.create(currentUser, request))
                .isInstanceOf(BusinessAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should create business with optional description")
        void shouldCreateBusinessWithOptionalDescription() {
            // Arrange
            BusinessRequest request = createBusinessRequest("Entreprise A", null, "Contact");
            when(businessRepository.existsByNameAndUser_Id("Entreprise A", userId)).thenReturn(false);
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.create(currentUser, request);

            // Assert
            assertThat(result.name()).isEqualTo("Entreprise A");
            assertThat(result.description()).isNull();
        }
    }

    /* ---------------------------------------------------------
        TESTS UPDATE BUSINESS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("updateBusiness()")
    class UpdateBusinessTests {

        @Test
        @DisplayName("Should update business with same name")
        void shouldUpdateBusinessWithSameName() {
            // Arrange
            Business existing = createBusiness("Entreprise A", "Old Description", "Old Contact", currentUser);
            BusinessRequest request = createBusinessRequest("Entreprise A", "New Description", "New Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existing));
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.updateBusiness(currentUser, 1L, request);

            // Assert
            assertThat(result.name()).isEqualTo("Entreprise A");
            assertThat(result.description()).isEqualTo("New Description");
            assertThat(result.recruitmentServiceContact()).isEqualTo("New Contact");
        }

        @Test
        @DisplayName("Should update business with new unique name")
        void shouldUpdateBusinessWithNewUniqueName() {
            // Arrange
            Business existing = createBusiness("Entreprise A", "Description", "Contact", currentUser);
            BusinessRequest request = createBusinessRequest("Entreprise B", "Description", "Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existing));
            when(businessRepository.existsByNameAndUser_Id("Entreprise B", userId)).thenReturn(false);
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.updateBusiness(currentUser, 1L, request);

            // Assert
            assertThat(result.name()).isEqualTo("Entreprise B");
        }

        @Test
        @DisplayName("Should throw BusinessAlreadyExistsException when new name already exists")
        void shouldThrowExceptionWhenNewNameExists() {
            // Arrange
            Business existing = createBusiness("Entreprise A", "Description", "Contact", currentUser);
            BusinessRequest request = createBusinessRequest("Entreprise B", "Description", "Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existing));
            when(businessRepository.existsByNameAndUser_Id("Entreprise B", userId)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> businessService.updateBusiness(currentUser, 1L, request))
                .isInstanceOf(BusinessAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business not found")
        void shouldThrowExceptionWhenBusinessNotFound() {
            // Arrange
            BusinessRequest request = createBusinessRequest("Entreprise A", "Description", "Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> businessService.updateBusiness(currentUser, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business belongs to another user")
        void shouldThrowExceptionWhenBusinessBelongsToAnotherUser() {
            // Arrange
            BusinessRequest request = createBusinessRequest("Entreprise A", "Description", "Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> businessService.updateBusiness(currentUser, 1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should trim business name before update")
        void shouldTrimBusinessNameBeforeUpdate() {
            // Arrange
            Business existing = createBusiness("Entreprise A", "Description", "Contact", currentUser);
            BusinessRequest request = createBusinessRequest("  Entreprise B  ", "Description", "Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existing));
            when(businessRepository.existsByNameAndUser_Id("Entreprise B", userId)).thenReturn(false);
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.updateBusiness(currentUser, 1L, request);

            // Assert
            assertThat(result.name()).isEqualTo("Entreprise B");
        }

        @Test
        @DisplayName("Should update all fields even when name unchanged")
        void shouldUpdateAllFieldsWhenNameUnchanged() {
            // Arrange
            Business existing = createBusiness("Entreprise A", "Old Desc", "Old Contact", currentUser);
            BusinessRequest request = createBusinessRequest("Entreprise A", "New Desc", "New Contact");
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(existing));
            when(businessRepository.save(any(Business.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            BusinessResponse result = businessService.updateBusiness(currentUser, 1L, request);

            // Assert
            assertThat(result.description()).isEqualTo("New Desc");
            assertThat(result.recruitmentServiceContact()).isEqualTo("New Contact");
        }
    }

    /* ---------------------------------------------------------
        TESTS DELETE BUSINESS
     --------------------------------------------------------- */

    @Nested
    @DisplayName("deleteBusiness()")
    class DeleteBusinessTests {

        @Test
        @DisplayName("Should delete existing business owned by user")
        void shouldDeleteExistingBusinessOwnedByUser() {
            // Arrange
            Business business = createBusiness("Entreprise A", "Description", "Contact", currentUser);
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(business));

            // Act
            businessService.deleteBusiness(currentUser, 1L);

            // Assert
            verify(businessRepository).findByIdAndUserId(1L, userId);
            verify(businessRepository).delete(business);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business not found")
        void shouldThrowExceptionWhenBusinessNotFound() {
            // Arrange
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> businessService.deleteBusiness(currentUser, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when business owned by another user")
        void shouldThrowExceptionWhenBusinessOwnedByAnotherUser() {
            // Arrange
            when(businessRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> businessService.deleteBusiness(currentUser, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existing business id")
        void shouldThrowExceptionForNonExistingBusinessId() {
            // Arrange
            when(businessRepository.findByIdAndUserId(999L, userId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> businessService.deleteBusiness(currentUser, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
        }
    }
}
