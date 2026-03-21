package com.GDP.GDP.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.security.CustomUserDetails;
import com.GDP.GDP.service.business.BusinessService;

@RestController
@RequestMapping("/api/business")
public class BusinessController {
    private final BusinessService businessService;

    public BusinessController(BusinessService businessService){
        this.businessService = businessService;
    }

    @GetMapping()
    public ResponseEntity<List<BusinessResponse>> getByUserId(@AuthenticationPrincipal CustomUserDetails user){
        return ResponseEntity.ok(businessService.getBusinessByUserId(user.getUser()));
    }    
    
}
