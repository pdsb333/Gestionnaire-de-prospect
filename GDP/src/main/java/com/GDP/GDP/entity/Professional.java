package com.GDP.GDP.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "professionals")
public class Professional {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String lastName;

    @Column(nullable=false)
    private String firstName;

    @Column(nullable=false)
    private String job;

    @Column(nullable=false)
    private String contact;

    @ManyToOne
    @JoinColumn(name="business_id", nullable=false)
    private Business business;

    public Professional(){
    }

    public Professional(String lastName, String firstName, String job, String contact, Business business) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.job = job;
        this.contact = contact;
        this.business = business;
    }

    // Getters and Setters

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getLastName(){
        return lastName;
    }

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getFirstName(){
        return firstName;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public String getJob(){
        return job;
    }

    public void setJob(String job){
        this.job = job;
    }

    public String getContact(){
        return contact;
    }

    public void setContact(String contact){
        this.contact = contact;
    }

    public Business getBusiness(){
        return business;
    }

    public void setBusiness(Business business){
        this.business = business;
    }
}
