package com.GDP.GDP.dto.application;

import java.time.LocalDateTime;
import java.util.List;

import com.GDP.GDP.dto.prospect.ProspectRequest;

public record ApplicationRequest(
    ProspectRequest prospectData
) {
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