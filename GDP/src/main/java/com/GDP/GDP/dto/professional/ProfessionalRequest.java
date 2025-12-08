package com.GDP.GDP.dto.professional;

import jakarta.validation.constraints.NotBlank;

public class ProfessionalRequest {
    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    @NotBlank
    private String job;

    @NotBlank
    private String contact;

    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public String getLastName(){
        return this.lastName;
    }

    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    public String getFirstName(){
        return this.firstName;
    }

    public void setJob(String job){
        this.job = job;
    }

    public String getJob(){
        return this.job;
    }

    public void setContact(String contact){
        this.contact = contact;
    }

    public String getContact(){
        return this.contact;
    }
}
