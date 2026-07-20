package com.GDP.GDP.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OrderBy;

@MappedSuperclass
public abstract class Prospect {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDateTime initialApplicationDate;

    @Column(nullable=true)
    private LocalDateTime dateRelaunch;

    // Guarantees chronological (ascending) iteration order on every load, so consumers relying
    // on array order — e.g. the frontend's "most recent first" display via .reverse() — get a
    // stable result instead of whatever order the DB happens to return.
    @ElementCollection
    @OrderBy
    private Set<LocalDateTime> historyOfRelaunches = new LinkedHashSet<>();

    public Prospect(){}
    
    public Prospect(LocalDateTime initialApplicationDate, LocalDateTime dateRelaunch){
        this.initialApplicationDate = truncate(initialApplicationDate);
        this.dateRelaunch = truncate(dateRelaunch);
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
        this.initialApplicationDate = truncate(initialApplicationDate);
    }

    public LocalDateTime getDateRelaunch(){
        return dateRelaunch;
    }

    public void setDateRelaunch(LocalDateTime dateRelaunch){
        this.dateRelaunch = truncate(dateRelaunch);
    }

    public Set<LocalDateTime> getHistoryOfRelaunches(){
        return historyOfRelaunches;
    }

    public void addRelaunch(LocalDateTime date) {
        this.historyOfRelaunches.add(truncate(date));
    }

    // Columns are timestamp(6) (microsecond precision) in Postgres; truncating here keeps
    // in-memory equals()/hashCode() (used by the historyOfRelaunches Set) consistent with what
    // gets persisted and reloaded, instead of drifting on sub-microsecond JVM clock noise.
    private static LocalDateTime truncate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.truncatedTo(ChronoUnit.MICROS);
    }
}
