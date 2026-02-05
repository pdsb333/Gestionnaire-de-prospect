package com.GDP.GDP.dto.professional;

import jakarta.validation.constraints.NotBlank;

public record ProfessionalRequest(
    @NotBlank String lastName,
    @NotBlank String firstName,
    @NotBlank String job,
    @NotBlank String contact
) {}