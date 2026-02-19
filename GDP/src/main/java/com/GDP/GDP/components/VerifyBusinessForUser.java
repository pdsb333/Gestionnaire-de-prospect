package com.GDP.GDP.components;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.ResourceNotFoundException;
import com.GDP.GDP.repository.BusinessRepository;

@Component
public class VerifyBusinessForUser {

    private final BusinessRepository businessRepository;

    public VerifyBusinessForUser(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public Business verifyBusiness(Long id, User user) {
        UUID currentUserId = user.getId();

        Business business = businessRepository
            .findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Business", id));
        return business;
    }
}