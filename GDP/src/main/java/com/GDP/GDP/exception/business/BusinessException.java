package com.GDP.GDP.exception.business;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus httpStatus; 

    public BusinessException(String code, String defaultMessage, HttpStatus status) {
        super(defaultMessage);
        this.code = code;
        this.httpStatus = status;
  
    }

    public String getCode() {
        return code;
    }
    public HttpStatus geHttpStatus(){
        return httpStatus;
    }
}

