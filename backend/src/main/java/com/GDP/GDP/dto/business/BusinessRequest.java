package com.GDP.GDP.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessRequest(
    @NotBlank(message = "Name is required") @Size(max = 255) String name,
    @NotBlank @Size(max = 255) String description,
    @NotBlank @Size(max = 255) String recruitmentServiceContact
){}