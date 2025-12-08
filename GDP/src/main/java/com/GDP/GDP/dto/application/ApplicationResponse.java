package com.GDP.GDP.dto.application;

import com.GDP.GDP.dto.prospect.ProspectResponse;
import com.GDP.GDP.entity.Application;


public class ApplicationResponse extends ProspectResponse {
    public static ApplicationResponse fromEntity(Application application){
        ApplicationResponse dto = new ApplicationResponse();
        dto.copyFrom(application);
        return dto;
    }
}

