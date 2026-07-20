package com.GDP.GDP.dto.professional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfessionalRequest(
    @NotBlank @Size(max = 255) String lastName,
    @NotBlank @Size(max = 255) String firstName,
    @NotBlank @Size(max = 255) String job,
    @NotBlank @Size(max = 255) String contact
) {}