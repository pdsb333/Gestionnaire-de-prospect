package com.GDP.GDP.exception;

import org.springframework.http.HttpStatus;

import com.GDP.GDP.exception.business.BusinessException;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(
            "INVALID_CREDENTIALS",
            "Email ou mot de passe incorrect",
            HttpStatus.UNAUTHORIZED
        );
    }
}
