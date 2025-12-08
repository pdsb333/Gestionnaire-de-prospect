package com.GDP.GDP.dto.professional;

import com.GDP.GDP.entity.Professional;


public class ProfessionalResponse {
    
    private String lastName;
    private String firstName;
    private String job;
    private String contact;

    public static ProfessionalResponse fromEntity(Professional professional){
        ProfessionalResponse dto = new ProfessionalResponse();
        dto.lastName = professional.getLastName();
        dto.firstName = professional.getFirstName();
        dto.job = professional.getJob();
        dto.contact = professional.getContact();
        return dto;
    }

    public String getLastName(){
        return this.lastName;
    }

    public String getFirstName(){
        return this.firstName;
    }

    public String getJob(){
        return this.job;
    }

    public String getContact(){
        return this.contact;
    }

}
