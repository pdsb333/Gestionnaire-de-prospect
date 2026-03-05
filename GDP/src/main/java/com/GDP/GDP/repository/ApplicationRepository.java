package com.GDP.GDP.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.GDP.GDP.entity.Application;


@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {    
}
