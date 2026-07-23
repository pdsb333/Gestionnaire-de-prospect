package com.GDP.GDP.service.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ApplicationServiceImpl(ApplicationRepository applicationRepository, JobOfferRepository jobOfferRepository){
        this.applicationRepository = applicationRepository;
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

    private LocalDateTime lastRelaunch(Application application) {
        return application.getHistoryOfRelaunches().stream()
                .max(LocalDateTime::compareTo)
                .orElse(application.getInitialApplicationDate());
    }

    private Application verifyApplication(Long id, User user){
        return applicationRepository.findByIdAndOffer_Business_UserId(id, user.getId())
                                .orElseThrow(()-> new ResourceNotFoundException("Application", id));
    }

    @Override
    public ApplicationResponse create(ApplicationRequest request, Long jobOfferId, User user){
        // Ownership-scoped lookup: a jobOfferId that exists but belongs to another user must be
        // indistinguishable from one that doesn't exist at all, so ownership never leaks via the
        // error message.
        JobOffer jobOffer = jobOfferRepository.findByIdAndBusiness_UserId(jobOfferId, user.getId())
                                .orElseThrow(()-> new ResourceNotFoundException("JobOffer", jobOfferId));

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

        try {
            return ApplicationResponse.fromEntity(applicationRepository.save(application));
        } catch (DataIntegrityViolationException e) {
            // The jobOffer.getApplication() check above isn't atomic with this insert: two
            // concurrent creates on the same jobOffer can both pass it before either commits.
            // The DB's unique constraint on job_offer_id is the real guard in that race — this
            // just makes the loser's error consistent with the non-racy case instead of a
            // generic DATA_INTEGRITY_VIOLATION.
            throw new ApplicationAlreadyExistsException(jobOfferId);
        }
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

        // dateRelaunch is derived from the most recent relaunch actually recorded (never a
        // client-supplied value): falling back to initialApplicationDate only when no relaunch
        // has happened yet, so editing an application no longer rewinds progress already made
        // via markRelaunched.
        application.setDateRelaunch(computeRelaunch(lastRelaunch(application), application.getOffer().getRelaunchFrequency()));

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
