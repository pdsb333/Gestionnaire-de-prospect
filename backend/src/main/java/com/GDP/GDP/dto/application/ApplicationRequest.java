package com.GDP.GDP.dto.application;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;


public record ApplicationRequest(
    @NotNull LocalDateTime initialApplicationDate
) {}