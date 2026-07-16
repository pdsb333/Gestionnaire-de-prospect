package com.GDP.GDP.service;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.application.ApplicationRequest;
import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.*;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.ApplicationRepository;
import com.GDP.GDP.repository.JobOfferRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.GDP.GDP.service.application.ApplicationServiceImpl;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobOfferRepository jobOfferRepository;

    @Mock
    private VerifyBusinessForUser verifyBusinessForUser;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    private User user;
    private Business business;
    private JobOffer jobOffer;
    private Application application;

    private final LocalDateTime initialDate  = LocalDateTime.of(2024, 1, 1, 10, 0);
    private final LocalDateTime relaunchDate = LocalDateTime.of(2024, 1, 8, 10, 0);

    @BeforeEach
    void setUp() {
        user = new User("testUser", "test@test.com", "password", User.Role.ROLE_USER);
        user.setId(UUID.randomUUID());

        business = new Business("Acme Corp", "desc", "rh@acme.com", user);
        business.setId(1L);

        jobOffer = new JobOffer("Dev Java", "http://offer.com", 7, business);
        jobOffer.setId(1L);

        application = new Application(initialDate, relaunchDate, jobOffer);
        application.setId(1L);
        application.addRelaunch(initialDate);
    }


    /* ---------------------------------------------------------
        TESTS CREATE APPLICATION
    --------------------------------------------------------- */

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should compute relaunch date from jobOffer relaunchFrequency")
        void shouldComputeRelaunchDateFromFrequency() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user)).thenReturn(business);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            ApplicationResponse response = applicationService.create(request, 1L, user);

            // relaunchFrequency = 7 → initialDate + 7 days
            assertThat(response.dateRelaunch()).isEqualTo(initialDate.plusDays(7));
        }

        @Test
        @DisplayName("should fallback to plusDays(1) when relaunchFrequency is 0")
        void shouldFallbackToOneDayWhenFrequencyIsZero() {
            jobOffer.setRelaunchFrequency(0);
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user)).thenReturn(business);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            ApplicationResponse response = applicationService.create(request, 1L, user);

            assertThat(response.dateRelaunch()).isEqualTo(initialDate.plusDays(1));
        }

        @Test
        @DisplayName("should fallback to plusDays(1) when relaunchFrequency is negative")
        void shouldFallbackToOneDayWhenFrequencyIsNegative() {
            jobOffer.setRelaunchFrequency(-5);
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user)).thenReturn(business);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            ApplicationResponse response = applicationService.create(request, 1L, user);

            assertThat(response.dateRelaunch()).isEqualTo(initialDate.plusDays(1));
        }

        @Test
        @DisplayName("should add initialApplicationDate to relaunch history")
        void shouldAddInitialDateToRelaunchHistory() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user)).thenReturn(business);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            ApplicationResponse response = applicationService.create(request, 1L, user);

            assertThat(response.historyOfRelaunches()).containsExactly(initialDate);
        }

        @Test
        @DisplayName("should call save() exactly once")
        void shouldCallSaveOnce() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user)).thenReturn(business);
            when(applicationRepository.save(any(Application.class))).thenAnswer(inv -> inv.getArgument(0));

            applicationService.create(request, 1L, user);

            verify(applicationRepository, times(1)).save(any(Application.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when jobOffer does not exist")
        void shouldThrowWhenJobOfferNotFound() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.create(request, 99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException e = (ResourceNotFoundException) ex;
                    assertThat(e.getResource()).isEqualTo("JobOffer");
                    assertThat(e.getIdentifier()).isEqualTo(99L);
                });

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when business does not belong to user")
        void shouldThrowWhenBusinessNotOwnedByUser() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(jobOfferRepository.findById(1L)).thenReturn(Optional.of(jobOffer));
            when(verifyBusinessForUser.verifyBusiness(1L, user))
                .thenThrow(new ResourceNotFoundException("Business", 1L));

            assertThatThrownBy(() -> applicationService.create(request, 1L, user))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(applicationRepository, never()).save(any());
        }
    }

    /* ---------------------------------------------------------
        TESTS UPDATE APPLICATION
    --------------------------------------------------------- */

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should recompute dateRelaunch from frequency when initialApplicationDate is unchanged, never trusting client input")
        void shouldRecomputeRelaunchDateWhenInitialDateUnchanged() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.update(request, 1L, user);

            // relaunchFrequency = 7 → initialDate + 7 days, regardless of what was requested before
            assertThat(response.dateRelaunch()).isEqualTo(initialDate.plusDays(7));
            assertThat(response.initialApplicationDate()).isEqualTo(initialDate);
            assertThat(response.historyOfRelaunches()).containsExactly(initialDate);
        }

        @Test
        @DisplayName("should replace old initialDate with new one in relaunch history")
        void shouldReplaceOldInitialDateInHistoryWhenInitialDateChanges() {
            LocalDateTime newInitialDate = LocalDateTime.of(2024, 2, 1, 10, 0);
            ApplicationRequest request = new ApplicationRequest(newInitialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.update(request, 1L, user);

            assertThat(response.historyOfRelaunches()).contains(newInitialDate);
            assertThat(response.historyOfRelaunches()).doesNotContain(initialDate);
        }

        @Test
        @DisplayName("should recompute dateRelaunch via computeRelaunch when initialApplicationDate changes")
        void shouldRecomputeRelaunchDateWhenInitialDateChanges() {
            LocalDateTime newInitialDate = LocalDateTime.of(2024, 2, 1, 10, 0);
            ApplicationRequest request = new ApplicationRequest(newInitialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.update(request, 1L, user);

            // relaunchFrequency = 7
            assertThat(response.dateRelaunch()).isEqualTo(newInitialDate.plusDays(7));
        }

        @Test
        @DisplayName("should update initialApplicationDate when it changes")
        void shouldUpdateInitialApplicationDateWhenChanged() {
            LocalDateTime newInitialDate = LocalDateTime.of(2024, 2, 1, 10, 0);
            ApplicationRequest request = new ApplicationRequest(newInitialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.update(request, 1L, user);

            assertThat(response.initialApplicationDate()).isEqualTo(newInitialDate);
        }

        @Test
        @DisplayName("should not call save() explicitly — Hibernate dirty checking")
        void shouldNotCallSaveExplicitly() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            applicationService.update(request, 1L, user);

            verify(applicationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when application does not exist or does not belong to user")
        void shouldThrowWhenApplicationNotFound() {
            ApplicationRequest request = new ApplicationRequest(initialDate);
            when(applicationRepository.findByIdAndOffer_Business_UserId(99L, user.getId()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.update(request, 99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException e = (ResourceNotFoundException) ex;
                    assertThat(e.getResource()).isEqualTo("Application");
                    assertThat(e.getIdentifier()).isEqualTo(99L);
                });
        }
    }
    /* ---------------------------------------------------------
        TESTS DELETE APPLICATION
    --------------------------------------------------------- */

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should call delete with the found entity")
        void shouldCallDeleteWithFoundEntity() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            applicationService.delete(1L, user);

            verify(applicationRepository).delete(application);
        }

        @Test
        @DisplayName("should not call delete when application does not exist or does not belong to user")
        void shouldNotDeleteWhenApplicationNotFound() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(99L, user.getId()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.delete(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException e = (ResourceNotFoundException) ex;
                    assertThat(e.getResource()).isEqualTo("Application");
                    assertThat(e.getIdentifier()).isEqualTo(99L);
                });

            verify(applicationRepository, never()).delete(any());
        }
    }

    /* ---------------------------------------------------------
        TESTS MARK RELAUNCHED
    --------------------------------------------------------- */

    @Nested
    @DisplayName("markRelaunched()")
    class MarkRelaunched {

        @Test
        @DisplayName("should truncate LocalDateTime.now() to microsecond precision (Postgres timestamp(6))")
        void shouldTruncateNowToMicrosecondPrecision() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.markRelaunched(1L, user);

            assertThat(response.dateRelaunch().getNano() % 1000).isZero();
            assertThat(response.historyOfRelaunches())
                .allSatisfy(date -> assertThat(date.getNano() % 1000).isZero());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when application does not exist or does not belong to user")
        void shouldThrowWhenApplicationNotFound() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(99L, user.getId()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> applicationService.markRelaunched(99L, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException e = (ResourceNotFoundException) ex;
                    assertThat(e.getResource()).isEqualTo("Application");
                    assertThat(e.getIdentifier()).isEqualTo(99L);
                });
        }

        @Test
        @DisplayName("should add the current time to relaunch history")
        void shouldAddCurrentTimeToHistory() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            LocalDateTime before = LocalDateTime.now();
            ApplicationResponse response = applicationService.markRelaunched(1L, user);
            LocalDateTime after = LocalDateTime.now();

            assertThat(response.historyOfRelaunches()).hasSize(2); // initialDate (from setUp) + new relaunch
            assertThat(response.historyOfRelaunches())
                .anySatisfy(date -> assertThat(date).isBetween(before, after));
        }

        @Test
        @DisplayName("should compute dateRelaunch from now + relaunchFrequency")
        void shouldComputeDateRelaunchFromNowPlusFrequency() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            LocalDateTime before = LocalDateTime.now();
            ApplicationResponse response = applicationService.markRelaunched(1L, user);
            LocalDateTime after = LocalDateTime.now();

            // relaunchFrequency = 7
            assertThat(response.dateRelaunch()).isBetween(before.plusDays(7), after.plusDays(7));
        }

        @Test
        @DisplayName("should fallback to plusDays(1) when relaunchFrequency is 0")
        void shouldFallbackToOneDayWhenFrequencyIsZero() {
            jobOffer.setRelaunchFrequency(0);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            LocalDateTime before = LocalDateTime.now();
            ApplicationResponse response = applicationService.markRelaunched(1L, user);
            LocalDateTime after = LocalDateTime.now();

            assertThat(response.dateRelaunch()).isBetween(before.plusDays(1), after.plusDays(1));
        }

        @Test
        @DisplayName("should fallback to plusDays(1) when relaunchFrequency is negative")
        void shouldFallbackToOneDayWhenFrequencyIsNegative() {
            jobOffer.setRelaunchFrequency(-5);
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            LocalDateTime before = LocalDateTime.now();
            ApplicationResponse response = applicationService.markRelaunched(1L, user);
            LocalDateTime after = LocalDateTime.now();

            assertThat(response.dateRelaunch()).isBetween(before.plusDays(1), after.plusDays(1));
        }

        @Test
        @DisplayName("should not change initialApplicationDate")
        void shouldNotChangeInitialApplicationDate() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse response = applicationService.markRelaunched(1L, user);

            assertThat(response.initialApplicationDate()).isEqualTo(initialDate);
        }

        @Test
        @DisplayName("should accumulate successive relaunches in history instead of overwriting")
        void shouldAccumulateSuccessiveRelaunches() {
            when(applicationRepository.findByIdAndOffer_Business_UserId(1L, user.getId()))
                .thenReturn(Optional.of(application));

            ApplicationResponse first = applicationService.markRelaunched(1L, user);
            ApplicationResponse second = applicationService.markRelaunched(1L, user);

            // the second call's history must still contain everything the first call had
            // (a Set never loses entries via add(), even if two calls happen to land on the
            // same truncated instant)
            assertThat(second.historyOfRelaunches()).containsAll(first.historyOfRelaunches());
            assertThat(second.historyOfRelaunches().size())
                .isGreaterThanOrEqualTo(first.historyOfRelaunches().size());
        }
    }
}