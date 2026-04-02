package com.GDP.GDP.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.Application.ApplicationRequest;
import com.GDP.GDP.dto.Application.ApplicationResponse;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.service.application.ApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService){
        this.applicationService = applicationService;
    }

    @PostMapping("/{jobOfferId}") 
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request, @PathVariable Long jobOfferId, @AuthenticationPrincipal CustomUserDetails user ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(applicationService.create(request, jobOfferId, user.getUser()));
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<ApplicationResponse> update(@PathVariable Long applicationId, @Valid @RequestBody ApplicationRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(applicationService.update(request, applicationId, user.getUser()));  
    }
   
}
