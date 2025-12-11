package com.GDP.GDP.dto.joboffer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class JobOfferRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String link;

    @Min(1)
    private Integer relaunchFrequency;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setLink(String link){
        this.link = link;
    }

    public String getLink(){
        return this.link;
    }

    public Integer getRelaunchFrequency(){
        return this.relaunchFrequency ;
    }

    public void setRelaunchFrequency(Integer relaunchFrequency){
        this.relaunchFrequency = relaunchFrequency;
    }
}
