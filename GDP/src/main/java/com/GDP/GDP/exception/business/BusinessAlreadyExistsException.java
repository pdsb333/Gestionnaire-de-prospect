package com.GDP.GDP.exception.business;

import org.springframework.http.HttpStatus;

public class BusinessAlreadyExistsException extends BusinessException {
    public BusinessAlreadyExistsException(String name) {
        super("BUSINESS_ALREADY_EXISTS",
                   "Business '" + name + "' already exists",
                    HttpStatus.CONFLICT
        );
    }
}