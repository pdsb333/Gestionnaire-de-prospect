package com.GDP.GDP.entity;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Prospect {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDateTime initialApplicationDate;

    @Column(nullable=true)
    private LocalDateTime dateRelaunch;

    @ElementCollection
    private Set<LocalDateTime> historyOfRelaunches = new LinkedHashSet<>();

    public Prospect(){}
    
    public Prospect(LocalDateTime initialApplicationDate, LocalDateTime dateRelaunch){
        this.initialApplicationDate = initialApplicationDate;
        this.dateRelaunch = dateRelaunch;
        this.historyOfRelaunches = new LinkedHashSet<>();    
    }
        
    // Getters and Setters
    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public LocalDateTime getInitialApplicationDate(){
        return initialApplicationDate;
    }
    public void setInitialApplicationDate(LocalDateTime initialApplicationDate){
        this.initialApplicationDate = initialApplicationDate;   
    }

    public LocalDateTime getDateRelaunch(){
        return dateRelaunch;
    }

    public void setDateRelaunch(LocalDateTime dateRelaunch){
        this.dateRelaunch = dateRelaunch;
    }

    public Set<LocalDateTime> getHistoryOfRelaunches(){
        return historyOfRelaunches;
    }

}
