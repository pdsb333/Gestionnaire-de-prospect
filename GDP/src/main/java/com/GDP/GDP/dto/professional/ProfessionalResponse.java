package com.GDP.GDP.dto.professional;

import com.GDP.GDP.entity.Professional;

public record ProfessionalResponse(
    Long id,
    String lastName,
    String firstName,
    String job,
    String contact
) {
    public static ProfessionalResponse fromEntity(Professional professional) {
        return new ProfessionalResponse(
            professional.getId(),
            professional.getLastName(),
            professional.getFirstName(),
            professional.getJob(),
            professional.getContact()
        );
    }
}