package com.GDP.GDP.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GDP.GDP.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long>{
    List<Business> findByUser_Id(UUID userId);
    Optional<Business> findByIdAndUserId(Long businessId, UUID userId);
    boolean existsByNameAndUser_Id(String name, UUID userId);
}