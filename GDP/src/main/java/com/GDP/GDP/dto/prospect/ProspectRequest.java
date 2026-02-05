package com.GDP.GDP.dto.prospect;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProspectRequest(
    @NotBlank LocalDateTime initialApplicationDate,
    @NotBlank LocalDateTime dateRelaunch,
    @NotNull List<LocalDateTime> historyOfRelaunches
) {}