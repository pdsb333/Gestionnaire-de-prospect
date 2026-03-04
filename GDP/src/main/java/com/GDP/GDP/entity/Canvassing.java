package com.GDP.GDP.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "canvassings")
public class Canvassing extends Prospect {
    
    public Canvassing(){
        super();
    }

    public Canvassing(java.time.LocalDateTime initialApplicationDate, java.time.LocalDateTime dateRelaunch) {
        super(initialApplicationDate, dateRelaunch);
    }
}
