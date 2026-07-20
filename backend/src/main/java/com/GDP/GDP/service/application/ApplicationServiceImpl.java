package com.GDP.GDP.service.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.application.ApplicationRequest;
import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.Application;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.exception.business.ApplicationAlreadyExistsException;
import com.GDP.GDP.exception.business.FutureDateException;
import com.GDP.GDP.repository.ApplicationRepository;
import com.GDP.GDP.repository.JobOfferRepository;

@Service
@Transactional
public class ApplicationServiceImpl implements ApplicationService{
    private final ApplicationRepository applicationRepository;
    private final JobOfferRepository jobOfferRepository;
    private final VerifyBusinessForUser verifyBusinessForUser;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, VerifyBusinessForUser verifyBusinessForUser, JobOfferRepository jobOfferRepository){
        this.applicationRepository = applicationRepository;
        this.verifyBusinessForUser = verifyBusinessForUser;
        this.jobOfferRepository = jobOfferRepository;
    }

    private LocalDateTime computeRelaunch(LocalDateTime date, int frequency) {
        return date.plusDays(frequency < 1 ? 1 : frequency);
    }

    private void validateNotFuture(LocalDateTime date) {
        if (date.toLocalDate().isAfter(LocalDate.now())) {
            throw new FutureDateException();
        }
    }

    private Application verifyApplication(Long id, User user){
        return applicationRepository.findByIdAndOffer_Business_UserId(id, user.getId())
                                .orElseThrow(()-> new ResourceNotFoundException("Application", id));
    }

    @Override
    public ApplicationResponse create(ApplicationRequest request, Long jobOfferId, User user){
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                                .orElseThrow(()-> new ResourceNotFoundException("JobOffer", jobOfferId));
        verifyBusinessForUser.verifyBusiness(jobOffer.getBusiness().getId(), user);

        if (jobOffer.getApplication() != null) {
            throw new ApplicationAlreadyExistsException(jobOfferId);
        }

        // Truncate to microseconds (Postgres timestamp(6)) so the in-memory value used for
        // historyOfRelaunches Set membership matches exactly what gets persisted and reloaded.
        LocalDateTime initialDate = request.initialApplicationDate().truncatedTo(ChronoUnit.MICROS);
        validateNotFuture(initialDate);
        LocalDateTime dateRelaunch = computeRelaunch(initialDate, jobOffer.getRelaunchFrequency());

        Application application = new Application(initialDate, dateRelaunch, jobOffer);
        application.addRelaunch(initialDate);

        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

    @Override
    public ApplicationResponse update(ApplicationRequest request, Long id, User user){
        Application application = verifyApplication(id, user);
        LocalDateTime newInitialDate = request.initialApplicationDate().truncatedTo(ChronoUnit.MICROS);
        validateNotFuture(newInitialDate);

        if(!application.getInitialApplicationDate().equals(newInitialDate)){
            application.getHistoryOfRelaunches().remove(application.getInitialApplicationDate());
            application.setInitialApplicationDate(newInitialDate);
            application.addRelaunch(newInitialDate);
        }

        // dateRelaunch is always derived from initialApplicationDate + relaunchFrequency, same as
        // create() — never trust a client-supplied relaunch date.
        application.setDateRelaunch(computeRelaunch(newInitialDate, application.getOffer().getRelaunchFrequency()));

        return ApplicationResponse.fromEntity(application);
    }

    @Override
    public void delete(Long id, User user){
        Application application = verifyApplication(id, user);
        applicationRepository.delete(application);
    }

    @Override
    public ApplicationResponse markRelaunched(Long id, User user) {
        Application application = verifyApplication(id, user);
    
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        application.addRelaunch(now);
        application.setDateRelaunch(computeRelaunch(now, application.getOffer().getRelaunchFrequency()));
    
        return ApplicationResponse.fromEntity(application);
    }

}
