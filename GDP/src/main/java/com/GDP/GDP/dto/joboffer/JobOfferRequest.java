package com.GDP.GDP.dto.joboffer;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobOfferRequest( 
    @NotBlank String name,
    @URL String link,
    @Min(1) @NotNull Integer relaunchFrequency
){}
