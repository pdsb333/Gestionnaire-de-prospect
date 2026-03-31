package com.GDP.GDP.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.joboffer.JobOfferRequest;
import com.GDP.GDP.dto.joboffer.JobOfferResponse;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.service.jobOffer.JobOfferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/job-offers")
public class JobOfferController {
    private final JobOfferService jobOfferService;

    public JobOfferController(JobOfferService jobOfferService){
        this.jobOfferService = jobOfferService;
    }

    @PostMapping("/{businessId}")
    public ResponseEntity<JobOfferResponse> create(@Valid @RequestBody JobOfferRequest request, @PathVariable Long businessId, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(jobOfferService.create(request, businessId, user.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOfferResponse> updateJobOffer(@PathVariable Long id, @Valid @RequestBody JobOfferRequest request, @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(jobOfferService.updateJobOffer(request, id, user.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user){
        jobOfferService.deleteJobOffer(id, user.getUser());
        return ResponseEntity.noContent().build();
    }
    
}
