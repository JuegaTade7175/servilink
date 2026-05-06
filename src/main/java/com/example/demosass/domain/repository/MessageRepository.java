package com.example.demosass.domain.repository;

import com.example.demosass.domain.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByBookingIdOrderByCreatedAtAsc(Long bookingId);

    @Query("""
        SELECT m FROM Message m
        WHERE m.receiver.id = :userId AND m.isRead = false
    """)
    List<Message> findUnreadByReceiverId(@Param("userId") Long userId);

    @Modifying
    @Query("""
        UPDATE Message m SET m.isRead = true
        WHERE m.booking.id = :bookingId AND m.receiver.id = :userId
    """)
    int markBookingMessagesAsRead(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
