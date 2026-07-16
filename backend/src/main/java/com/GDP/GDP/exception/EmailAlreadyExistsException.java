package com.GDP.GDP.exception;

import org.springframework.http.HttpStatus;

import com.GDP.GDP.exception.business.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String email) {
        super(
            "EMAIL_ALREADY_EXISTS",
            "Email '" + email + "' already exists",
            HttpStatus.CONFLICT
        );
    }
}
