package com.GDP.GDP.service.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.application.ApplicationRequest;
import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.Application;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
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

    @Override
    public ApplicationResponse create(ApplicationRequest request, Long jobOfferId, User user){
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                                .orElseThrow(()-> new ResourceNotFoundException("JobOffer", jobOfferId));
        verifyBusinessForUser.verifyBusiness(jobOffer.getBusiness().getId(), user);

        LocalDateTime dateRelaunch = computeRelaunch(request.initialApplicationDate(), jobOffer.getRelaunchFrequency());

        Application application = new Application(
                    request.initialApplicationDate(),
                    dateRelaunch,
                    jobOffer);
        application.addRelaunch(request.initialApplicationDate());
        
        return ApplicationResponse.fromEntity(applicationRepository.save(application));
    }

}
