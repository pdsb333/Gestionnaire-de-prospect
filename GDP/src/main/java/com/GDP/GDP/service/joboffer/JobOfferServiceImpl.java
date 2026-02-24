package com.GDP.GDP.service.joboffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.JobOfferRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class JobOfferServiceImpl implements JobOfferService {
    private final JobOfferRepository jobOfferRepository;
    private final VerifyBusinessForUser verifyBusinessForUser;
    private static final Logger log = LoggerFactory.getLogger(JobOfferServiceImpl.class);


    public JobOfferServiceImpl(JobOfferRepository jobOfferRepository, VerifyBusinessForUser verifyBusinessForUser){
        this.jobOfferRepository = jobOfferRepository;
        this.verifyBusinessForUser = verifyBusinessForUser;
    }

    private JobOffer verifyJobOffer(Long id, User user){
        
        return jobOfferRepository.findByIdAndBusiness_UserId(id, user.getId())
                            .orElseThrow(() -> {
                                log.warn("JobOffer {} access denied or not found for user {}", id, user.getId());
                                return new ResourceNotFoundException("JobOffer", id);
                            });
    }

    @Override
    public JobOfferResponse create(JobOfferRequest request, Long businessId, User user){     
        
        Business business = verifyBusinessForUser.verifyBusiness(businessId, user);
        JobOffer jobOffer = new JobOffer(request.name(), request.link(), request.relaunchFrequency(), business);
        return JobOfferResponse.fromEntity(jobOfferRepository.save(jobOffer));
    }    
    
    @Override
    public JobOfferResponse updateJobOffer(JobOfferRequest request, Long id, User user){
        JobOffer jobOffer = verifyJobOffer(id, user);
        jobOffer.setName(request.name());
        jobOffer.setLink(request.link());
        jobOffer.setRelaunchFrequency(request.relaunchFrequency());
        return JobOfferResponse.fromEntity(jobOffer);
    }
}
