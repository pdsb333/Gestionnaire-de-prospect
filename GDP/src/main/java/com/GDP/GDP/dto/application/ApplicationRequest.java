package com.GDP.GDP.dto.application;

import java.time.LocalDateTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;


public record ApplicationRequest(
    @NotNull @PastOrPresent LocalDateTime initialApplicationDate,
    @NotNull @FutureOrPresent LocalDateTime dateRelaunch
) {}