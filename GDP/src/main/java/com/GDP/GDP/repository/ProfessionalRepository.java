package com.GDP.GDP.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GDP.GDP.entity.Business;
import com.GDP.GDP.entity.Professional;


@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long>{
    List<Professional> findByBusiness(Business business);
}
