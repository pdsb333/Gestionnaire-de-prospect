package com.GDP.GDP.dto.prospect;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;  

public abstract class ProspectRequest {
    
    @NotBlank
    private LocalDateTime initialApplicationDate;

    @NotBlank
    private LocalDateTime dateRelaunch;

    @NotNull
    private List<LocalDateTime> historyOfRelaunches;


    public void setInitialApplicationDate(LocalDateTime initialApplicationDate){
        this.initialApplicationDate = initialApplicationDate;
    }

    public LocalDateTime getInitialApplicationDate(){
        return this.initialApplicationDate;
    }

    public void setDateRelaunch(LocalDateTime dateRelaunch){
        this.dateRelaunch = dateRelaunch;
    }

    public LocalDateTime getDateRelaunch(){
        return this.dateRelaunch;
    }

    public void setHistoryOfRelaunches(List<LocalDateTime> historyOfRelaunches){
        this.historyOfRelaunches = historyOfRelaunches;
    }

    public List<LocalDateTime> getHistoryOfRelaunches(){
        return this.historyOfRelaunches;
    }
}

