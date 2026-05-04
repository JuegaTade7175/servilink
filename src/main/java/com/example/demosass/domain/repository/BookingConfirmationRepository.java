package com.example.demosass.domain.repository;

import com.example.demosass.domain.model.BookingConfirmation;
import com.example.demosass.domain.model.BookingConfirmation.ConfirmationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BookingConfirmationRepository extends JpaRepository<BookingConfirmation, Long> {

    Optional<BookingConfirmation> findByBookingId(Long bookingId);

    Optional<BookingConfirmation> findByConfirmationCodeAndStatus(
        String confirmationCode, ConfirmationStatus status
    );

    @Modifying
    @Query("""
        UPDATE BookingConfirmation bc
        SET bc.status = 'EXPIRED'
        WHERE bc.status = 'PENDING'
        AND bc.expiresAt < :now
    """)
    int expireOldConfirmations(@Param("now") LocalDateTime now);
}
