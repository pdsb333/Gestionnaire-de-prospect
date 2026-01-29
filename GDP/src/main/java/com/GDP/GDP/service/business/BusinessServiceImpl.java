package com.GDP.GDP.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.repository.BusinessRepository;

@Service
public class BusinessServiceImpl implements BusinessService {
    private final BusinessRepository businessRepository;

    public BusinessServiceImpl(BusinessRepository businessRepository){
        this.businessRepository = businessRepository;
    }

    @Override
    public List<BusinessResponse> getBusinessByUserId(User user){
        return businessRepository.findByUser_Id(user.getId())
                    .stream()
                    .map(BusinessResponse::fromEntity)
                    .collect(Collectors.toList());
    }
}
