package com.GDP.GDP.service.business;

import java.util.List;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.entity.User;

public interface BusinessService {
    BusinessResponse create(User user, BusinessRequest request);
    List<BusinessResponse> getBusinessByUserId(User user);
    BusinessResponse updateBusiness(User user, Long id, BusinessRequest request);
    //void deleteBusiness(User user, @NotNull Long id);
}
