package com.example.demosass.domain.repository;

import com.example.demosass.domain.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    Optional<Professional> findByUserId(Long userId);

    @Query("""
        SELECT p FROM Professional p
        WHERE p.latitude IS NOT NULL AND p.longitude IS NOT NULL
        AND (6371 * 2 * asin(sqrt(
            power(sin((radians(p.latitude) - radians(:lat)) / 2), 2)
            + cos(radians(:lat)) * cos(radians(p.latitude))
            * power(sin((radians(p.longitude) - radians(:lon)) / 2), 2)
        ))) <= :radiusKm
        ORDER BY (6371 * 2 * asin(sqrt(
            power(sin((radians(p.latitude) - radians(:lat)) / 2), 2)
            + cos(radians(:lat)) * cos(radians(p.latitude))
            * power(sin((radians(p.longitude) - radians(:lon)) / 2), 2)
        ))) ASC
    """)
    List<Professional> findNearbyProfessionals(
        @Param("lat") Double latitude,
        @Param("lon") Double longitude,
        @Param("radiusKm") Double radiusKm
    );

    @Query("""
        SELECT p FROM Professional p
        JOIN p.services s
        WHERE s.category.id = :categoryId
    """)
    List<Professional> findByCategoryId(@Param("categoryId") Long categoryId);

    @Query("""
        SELECT p FROM Professional p
        JOIN p.services s
        WHERE s.category.id = :categoryId
        AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL
        AND (6371 * 2 * asin(sqrt(
            power(sin((radians(p.latitude) - radians(:lat)) / 2), 2)
            + cos(radians(:lat)) * cos(radians(p.latitude))
            * power(sin((radians(p.longitude) - radians(:lon)) / 2), 2)
        ))) <= :radiusKm
        ORDER BY p.averageRating DESC
    """)
    List<Professional> findByCategoryAndLocation(
        @Param("categoryId") Long categoryId,
        @Param("lat") Double latitude,
        @Param("lon") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
}