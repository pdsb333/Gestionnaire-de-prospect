package com.GDP.GDP.exception.business;

import org.springframework.http.HttpStatus;

public class FutureDateException extends BusinessException {
    public FutureDateException() {
        super("INVALID_APPLICATION_DATE",
                    "La date de candidature ne peut pas être dans le futur",
                    HttpStatus.BAD_REQUEST
        );
    }
}
