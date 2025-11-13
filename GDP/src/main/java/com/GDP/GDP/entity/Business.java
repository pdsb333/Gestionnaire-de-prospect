package com.GDP.GDP.entity;


import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "businesses")
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private String recruitmentServiceContact;

    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @OneToMany(mappedBy="business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobOffer> jobOffersList = new ArrayList<>();

    public Business() {
    }
    public Business(String name, String description, String recruitmentServiceContact, User user
    ) {
        this.name = name;
        this.description = description;
        this.recruitmentServiceContact = recruitmentServiceContact;
        this.user = user;
    }
    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRecruitmentServiceContact() {
        return recruitmentServiceContact;
    }

    public void setRecruitmentServiceContact(String recruitmentServiceContact) {
        this.recruitmentServiceContact = recruitmentServiceContact;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<JobOffer> getJobOffersList(){
        return jobOffersList;
    }

    public void setJobOffersList(List<JobOffer> jobOffersList){
        this.jobOffersList = jobOffersList;
    }

    public void addJobOffer(JobOffer jobOffer) {
        jobOffersList.add(jobOffer);
        jobOffer.setBusiness(this);
    }

    public void removeJobOffer(JobOffer jobOffer) {
        jobOffersList.remove(jobOffer);
        jobOffer.setBusiness(null);
    }

}

