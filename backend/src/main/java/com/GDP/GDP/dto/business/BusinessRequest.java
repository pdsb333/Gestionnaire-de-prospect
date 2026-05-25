package com.GDP.GDP.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BusinessRequest( 
    @NotBlank(message = "Name is required") String name,
    @NotNull String description,
    @NotNull String recruitmentServiceContact
){}