package com.GDP.GDP.dto.joboffer;


import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.JobOffer;

public class JobOfferResponse {
    private String name;
    private String link;
    private Integer relaunchFrequency;
    private ApplicationResponse application;

    public static JobOfferResponse fromEntity(JobOffer jobOffer){
        JobOfferResponse dto = new JobOfferResponse();
        dto.name = jobOffer.getName();
        dto.link = jobOffer.getLink();
        dto.relaunchFrequency = jobOffer.getRelaunchFrequency();
        if (jobOffer.getApplication() != null) {
            dto.application = ApplicationResponse.fromEntity(jobOffer.getApplication());
        }        return dto;
    }

    public String getName(){
        return this.name;
    }

    public String getLink(){
        return this.link;
    }

    public Integer getRelaunchFrequency(){
        return this.relaunchFrequency;
    }

    public ApplicationResponse getApplication() {
        return this.application;
    }
}
