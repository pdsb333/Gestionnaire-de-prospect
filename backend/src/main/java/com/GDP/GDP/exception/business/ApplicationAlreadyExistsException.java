package com.GDP.GDP.exception.business;

import org.springframework.http.HttpStatus;

public class ApplicationAlreadyExistsException extends BusinessException {
    public ApplicationAlreadyExistsException(Long jobOfferId) {
        super("APPLICATION_ALREADY_EXISTS",
                    "Job offer '" + jobOfferId + "' already has an application",
                    HttpStatus.CONFLICT
        );
    }
}
