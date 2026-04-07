package com.GDP.GDP.service.professional;


import com.GDP.GDP.dto.Professional.ProfessionalRequest;
import com.GDP.GDP.dto.Professional.ProfessionalResponse;
import com.GDP.GDP.entity.User;

public interface ProfessionalService {
    ProfessionalResponse create(ProfessionalRequest request, Long businessId, User user);
    ProfessionalResponse updateProfessional(ProfessionalRequest request, Long id, User user);
    void deleteProfessional(Long id, User user);
}
