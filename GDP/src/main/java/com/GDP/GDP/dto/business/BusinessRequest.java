package com.GDP.GDP.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BusinessRequest {
    @NotBlank
    private String name;

    @NotNull
    private String description;

    @NotNull
    private String recruitmentServiceContact;

    //Getter and Setter

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getDescription(){
        return this.description;
    }

    public void setRecruitmentServiceContact(String recruitmentSerciveContact){
        this.recruitmentServiceContact = recruitmentSerciveContact;
    }

    public String getRecruitmentServiceContact(){
        return this.recruitmentServiceContact;
    }
}
