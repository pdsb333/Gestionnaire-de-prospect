package com.GDP.GDP.service.application;

import com.GDP.GDP.dto.application.ApplicationRequest;
import com.GDP.GDP.dto.application.ApplicationResponse;
import com.GDP.GDP.entity.User;

public interface ApplicationService {
    ApplicationResponse create(ApplicationRequest request, Long jobOfferId, User user);
    ApplicationResponse update(ApplicationRequest request, Long applicationId, User user);
    void delete(Long applicationId, User user);
}
