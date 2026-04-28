package com.GDP.GDP.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.service.business.BusinessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/business")
public class BusinessController {
    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping()
    public ResponseEntity<List<BusinessResponse>> getByUserId(@AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(businessService.getBusinessByUserId(user.getUser()));
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> create(@AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody BusinessRequest request) {
            
            URI location = URI.create("/api/business");
            return ResponseEntity.created(location).body(businessService.create(user.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> update(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long id, @Valid @RequestBody BusinessRequest request){
        return ResponseEntity.ok(businessService.updateBusiness(user.getUser(), id, request));
    }
}
