package com.GDP.GDP.exception;

import org.springframework.http.HttpStatus;

import com.GDP.GDP.exception.business.BusinessException;

public class ResourceNotFoundException extends BusinessException {

    private final String resource;
    private final Object identifier;

    public ResourceNotFoundException(String resource, Object identifier) {
        super(
            "RESOURCE_NOT_FOUND",
            resource + " not found with identifier: " + identifier,
            HttpStatus.NOT_FOUND
        );
        this.resource = resource;      
        this.identifier = identifier;  
    }

    public String getResource() {
        return resource;
    }

    public Object getIdentifier() {
        return identifier;
    }
}