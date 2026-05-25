package com.GDP.GDP.dto.business;

import java.util.List;

import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.dto.professional.ProfessionalResponse;
import com.GDP.GDP.entity.Business;

public record BusinessResponse(
    Long id,
    String name,
    String description,
    String recruitmentServiceContact,
    List<JobOfferResponse> jobOffersList,
    List<ProfessionalResponse> professionalsList
) {

    public static BusinessResponse fromEntity(Business business) {
        List<JobOfferResponse> jobOffers =
            business.getJobOffersList() == null
                ? List.of()
                : business.getJobOffersList()
                    .stream()
                    .map(JobOfferResponse::fromEntity)
                    .toList();

        List<ProfessionalResponse> professionals =
            business.getProfessionals() == null
                ? List.of()
                : business.getProfessionals()
                    .stream()
                    .map(ProfessionalResponse::fromEntity)
                    .toList();

        return new BusinessResponse(
            business.getId(),
            business.getName(),
            business.getDescription(),
            business.getRecruitmentServiceContact(),
            jobOffers,
            professionals
        );
    }
}
