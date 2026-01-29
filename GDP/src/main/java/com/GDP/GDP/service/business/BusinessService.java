package com.GDP.GDP.service.business;

import java.util.List;

import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.entity.User;

public interface BusinessService {
    //BusinessResponse create(User user, @Valid BusinessRequest request);
    List<BusinessResponse> getBusinessByUserId(User user);
    //BusinessResponse updateBusiness(User user, @NotNull Long id, @Valid BusinessRequest request);
    //void deleteBusiness(User user, @NotNull Long id);
}
