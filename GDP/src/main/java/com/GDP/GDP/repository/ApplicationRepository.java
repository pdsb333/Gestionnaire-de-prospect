package com.GDP.GDP.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GDP.GDP.entity.Application;


@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByIdAndOffer_Business_UserId(Long id, UUID userid);
    
}
