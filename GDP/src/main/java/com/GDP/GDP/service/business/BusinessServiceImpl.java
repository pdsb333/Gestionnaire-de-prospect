package com.GDP.GDP.service.business;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.GDP.GDP.dto.business.BusinessRequest;
import com.GDP.GDP.dto.business.BusinessResponse;
import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.User;
import com.GDP.GDP.exception.business.BusinessAlreadyExistsException;
import com.GDP.GDP.repository.BusinessRepository;

@Service
public class BusinessServiceImpl implements BusinessService {
    private final BusinessRepository businessRepository;

    public BusinessServiceImpl(BusinessRepository businessRepository){
        this.businessRepository = businessRepository;
    }
    private String normalizeBusinessName(String name) {
        return name.trim();
    }

    private void assertBusinessNameNotExists(User user, String name) {
        boolean businessExists =
            businessRepository.existsByNameAndUser_Id(
                name,
                user.getId()
            );

        if (businessExists) {
            throw new BusinessAlreadyExistsException(name);
        }
    }

    @Override
    public List<BusinessResponse> getBusinessByUserId(User user){
        return businessRepository.findByUser_Id(user.getId())
                    .stream()
                    .map(BusinessResponse::fromEntity)
                    .collect(Collectors.toList());
    }

    @Override
    public BusinessResponse create(User user, BusinessRequest request) {
        String normalized = normalizeBusinessName(request.getName());

        assertBusinessNameNotExists(user, normalized);

        Business business = new Business(
            normalized,
            request.getDescription(),
            request.getRecruitmentServiceContact(),
            user
        );

        return BusinessResponse.fromEntity(
            businessRepository.save(business)
        );
    }
}
