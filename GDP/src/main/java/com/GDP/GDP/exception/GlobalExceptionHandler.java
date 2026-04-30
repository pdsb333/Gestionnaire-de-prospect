package com.GDP.GDP.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.GDP.GDP.dto.ApiError;
import com.GDP.GDP.exception.business.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message, WebRequest request, Map<String, List<String>> validationErrors){
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            code,
            message,
            ((ServletWebRequest) request).getRequest().getRequestURI(),
            validationErrors
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            WebRequest request
    ) {
        return buildResponse(
            ex.geHttpStatus(),
            ex.getCode(),
            ex.getMessage(),
            request,
            null
        );
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        Map<String, List<String>> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors
                .computeIfAbsent(error.getField(), k -> new ArrayList<>())
                .add(error.getDefaultMessage())
        );

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            "VALIDATION_FAILED",
            "Validation failed",
            request,
            errors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, WebRequest request) {
        logger.error("Unexpected internal error", ex);
        return buildResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_ERROR",
            "An unexpected internal error occurred",
            request, 
            null);
    }   
    

}
