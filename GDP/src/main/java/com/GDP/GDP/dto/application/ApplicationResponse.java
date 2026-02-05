package com.GDP.GDP.dto.application;

import java.time.LocalDateTime;
import java.util.List;

import com.GDP.GDP.dto.prospect.ProspectResponse;
import com.GDP.GDP.entity.Application;

public record ApplicationResponse(
    ProspectResponse prospectData
) {
    public static ApplicationResponse fromEntity(Application application) {
        ProspectResponse prospectData = new ProspectResponse(
            application.getInitialApplicationDate(),
            application.getDateRelaunch(),
            application.getHistoryOfRelaunches()
        );
        return new ApplicationResponse(prospectData);
    }
    
    public LocalDateTime initialApplicationDate() {
        return prospectData.initialApplicationDate();
    }
    
    public LocalDateTime dateRelaunch() {
        return prospectData.dateRelaunch();
    }
    
    public List<LocalDateTime> historyOfRelaunches() {
        return prospectData.historyOfRelaunches();
    }
}