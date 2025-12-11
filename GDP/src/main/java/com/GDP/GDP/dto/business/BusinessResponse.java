package com.GDP.GDP.dto.business;

import java.util.List;

import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.dto.professional.ProfessionalResponse;
import com.GDP.GDP.entity.Business;

public class BusinessResponse {
    private Long id;
    private String name;
    private String description;
    private String recruitmentServiceContact;
    private List<JobOfferResponse> jobOffersList;
    private List<ProfessionalResponse> professionalsList;

    public static BusinessResponse fromEntity(Business business){
        BusinessResponse dto = new BusinessResponse();
        dto.id = business.getId();
        dto.name = business.getName();
        dto.description = business.getDescription();
        dto.recruitmentServiceContact = business.getRecruitmentServiceContact();
        dto.jobOffersList = business.getJobOffersList().stream()
            .map(JobOfferResponse::fromEntity)
            .toList();
        dto.professionalsList = business.getProfessionals().stream()
            .map(ProfessionalResponse::fromEntity)
            .toList();
        return dto;
    }

    public Long getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }
    public String getDescription(){
        return this.description;
    }

    public String getRecruitmentServiceContact(){
        return this.recruitmentServiceContact;
    }
    public List<JobOfferResponse> getJobOffersList(){
        return this.jobOffersList;
    }

    public List<ProfessionalResponse> getProfessionalsList(){
        return this.professionalsList;
    }
}
