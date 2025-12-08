package com.GDP.GDP.dto.prospect;

import java.time.LocalDateTime;
import java.util.List;

import com.GDP.GDP.entity.Prospect;


public abstract class ProspectResponse {
    private LocalDateTime initialApplicationDate;
    private LocalDateTime dateRelaunch;
    private List<LocalDateTime> historyOfRelaunches;

    protected void copyFrom(Prospect prospect){
        this.initialApplicationDate = prospect.getInitialApplicationDate();
        this.dateRelaunch = prospect.getDateRelaunch();
        this.historyOfRelaunches = prospect.getHistoryOfRelaunches();
    }

    public LocalDateTime getInitialApplicationDate(){
        return this.initialApplicationDate;
    }
    public LocalDateTime getDateRelaunch(){
        return this.dateRelaunch;
    }
    public List<LocalDateTime> getHistoryOfRelaunches(){
        return this.historyOfRelaunches;
    }
}
