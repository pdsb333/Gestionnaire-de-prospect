package com.GDP.GDP.dto.application;

import java.time.LocalDateTime;
import java.util.Set;

import com.GDP.GDP.entity.Application;

public record ApplicationResponse(
    Long id,
    LocalDateTime initialApplicationDate,
    LocalDateTime dateRelaunch,
    Set<LocalDateTime> historyOfRelaunches
) {
    public static ApplicationResponse fromEntity(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getInitialApplicationDate(),
            application.getDateRelaunch(),
            application.getHistoryOfRelaunches()
        );
    }
}