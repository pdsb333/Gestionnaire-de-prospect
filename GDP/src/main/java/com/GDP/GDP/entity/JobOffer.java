package com.GDP.GDP.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_offers")
public class JobOffer {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false)
    private String link;

    @Column(nullable=false)
    private Integer relaunchFrequency;

    @ManyToOne
    @JoinColumn(name="business_id", nullable=false)
    private Business business;

    @OneToOne(mappedBy="offer")
    private Application application;

    public JobOffer(){
    }
    public JobOffer(String name, String link, Integer relaunchFrequency ,Business business) {
        this.name = name;
        this.link = link;
        this.relaunchFrequency = relaunchFrequency;
        this.business = business;
    }

    // Getters and Setters
    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getLink(){
        return link;
    }

    public void setLink(String link){
        this.link = link;
    }

    public Integer getRelaunchFrequency(){
        return this.relaunchFrequency;
    }

    public void setRelaunchFrequency(Integer relaunchFrequency){
        this.relaunchFrequency = relaunchFrequency;
    }

    public Business getBusiness(){
        return business;
    }

    public void setBusiness(Business business){
        this.business = business;
    }

    public Application getApplication(){
        return application;
    }

    public void setApplication(Application application){
        this.application = application;
    }
}
