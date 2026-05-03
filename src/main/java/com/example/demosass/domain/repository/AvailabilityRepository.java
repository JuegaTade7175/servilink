package com.example.demosass.domain.repository;

import com.example.demosass.domain.enums.DayOfWeek;
import com.example.demosass.domain.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByProfessionalId(Long professionalId);
    List<Availability> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeek dayOfWeek);
}
