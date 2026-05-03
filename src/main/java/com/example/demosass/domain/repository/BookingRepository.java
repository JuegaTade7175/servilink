package com.example.demosass.domain.repository;

import com.example.demosass.domain.enums.BookingStatus;
import com.example.demosass.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByClientId(Long clientId);
    List<Booking> findByProfessionalId(Long professionalId);
    List<Booking> findByClientIdAndStatus(Long clientId, BookingStatus status);
    List<Booking> findByProfessionalIdAndStatus(Long professionalId, BookingStatus status);
}
