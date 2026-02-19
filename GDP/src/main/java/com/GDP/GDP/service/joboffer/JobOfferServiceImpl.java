package com.GDP.GDP.service.joboffer;

import org.springframework.stereotype.Service;

import com.GDP.GDP.components.VerifyBusinessForUser;
import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.JobOffer;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.repository.JobOfferRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class JobOfferServiceImpl implements JobOfferService {
    private final JobOfferRepository jobOfferRepository;
    private final VerifyBusinessForUser verifyBusinessForUser;

    public JobOfferServiceImpl(JobOfferRepository jobOfferRepository, VerifyBusinessForUser verifyBusinessForUser){
        this.jobOfferRepository = jobOfferRepository;
        this.verifyBusinessForUser = verifyBusinessForUser;
    }

    @Override
    public JobOfferResponse create(JobOfferRequest request, Long businessId, User user){     
        
        Business business = verifyBusinessForUser.verifyBusiness(businessId, user);
        JobOffer jobOffer = new JobOffer(request.name(), request.link(), request.relaunchFrequency(), business);
        return JobOfferResponse.fromEntity(jobOfferRepository.save(jobOffer));
    }   
}
