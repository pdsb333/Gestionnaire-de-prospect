package com.GDP.GDP.dto.prospect;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record ProspectRequest(
    @NotBlank @PastOrPresent LocalDateTime initialApplicationDate,
    @NotBlank @FutureOrPresent LocalDateTime dateRelaunch,
    @NotNull List<LocalDateTime> historyOfRelaunches
) {}