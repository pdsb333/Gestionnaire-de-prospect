package com.GDP.GDP.service.joboffer;


import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.entity.User;

public interface JobOfferService {
    JobOfferResponse create(JobOfferRequest request, Long businessId, User user);
    JobOfferResponse updateJobOffer(JobOfferRequest request, Long id, User user);
    //void deleteJobOffer(Long id, User user);
}
