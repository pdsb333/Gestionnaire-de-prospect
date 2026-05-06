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

import com.GDP.GDP.dto.professional.ProfessionalRequest;
import com.GDP.GDP.dto.professional.ProfessionalResponse;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.service.professional.ProfessionalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {
    private final ProfessionalService professionalService;

    public ProfessionalController(ProfessionalService professionalService){
        this.professionalService = professionalService;
    }

    @PostMapping("/{businessId}")
    public ResponseEntity<ProfessionalResponse> create(@Valid @RequestBody ProfessionalRequest request, @PathVariable Long businessId, @AuthenticationPrincipal CustomUserDetails user){
        return ResponseEntity.status(HttpStatus.CREATED)
                                .body(professionalService.create(request, businessId, user.getUser()));        
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> updateProfessional(@Valid @RequestBody ProfessionalRequest request, @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user){
        return ResponseEntity.ok(professionalService.updateProfessional(request, id, user.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user){
        professionalService.deleteProfessional(id, user.getUser());
        return ResponseEntity.noContent().build();
    }
    
}
