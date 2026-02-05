package com.GDP.GDP.dto.prospect;

import java.time.LocalDateTime;
import java.util.List;

public record ProspectResponse(
    LocalDateTime initialApplicationDate,
    LocalDateTime dateRelaunch,
    List<LocalDateTime> historyOfRelaunches
) {}
