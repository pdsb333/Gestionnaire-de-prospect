package com.GDP.GDP.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.GDP.GDP.entity.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long>{
    Optional<Business> findByIdAndUserId(Long businessId, UUID userId);
    boolean existsByNameAndUser_Id(String name, UUID userId);

    // jobOffersList and professionalsList are both @OneToMany Lists: fetch-joining both in a
    // single query throws MultipleBagFetchException, so they're loaded via two queries instead.
    // Both run in the same persistence context (see BusinessServiceImpl), so the second query
    // enriches the same managed Business instances the first one returned rather than requiring
    // a separate merge step.
    @Query("""
        SELECT DISTINCT b FROM Business b
        LEFT JOIN FETCH b.jobOffersList jo
        LEFT JOIN FETCH jo.application
        WHERE b.user.id = :userId
        """)
    List<Business> findByUserIdWithJobOffers(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT b FROM Business b
        LEFT JOIN FETCH b.professionalsList
        WHERE b.user.id = :userId
        """)
    List<Business> findByUserIdWithProfessionals(@Param("userId") UUID userId);
}