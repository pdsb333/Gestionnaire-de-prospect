package com.GDP.GDP.dto.joboffer;

import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.JobOffer;

public record JobOfferResponse(
    Long id,
    String name,
    String link,
    Integer relaunchFrequency,
    ApplicationResponse application
) {
    public static JobOfferResponse fromEntity(JobOffer jobOffer) {
        return new JobOfferResponse(
            jobOffer.getId(),
            jobOffer.getName(),
            jobOffer.getLink(),
            jobOffer.getRelaunchFrequency(),
            jobOffer.getApplication() != null 
                ? ApplicationResponse.fromEntity(jobOffer.getApplication())
                : null
        );
    }
}