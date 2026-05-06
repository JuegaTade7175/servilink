package com.example.demosass.service;

import com.example.demosass.domain.enums.NotificationType;
import com.example.demosass.domain.model.Booking;
import com.example.demosass.domain.model.Message;
import com.example.demosass.domain.model.User;
import com.example.demosass.domain.repository.BookingRepository;
import com.example.demosass.domain.repository.MessageRepository;
import com.example.demosass.domain.repository.UserRepository;
import com.example.demosass.dto.response.Responses.MessageResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public MessageResponse send(Long senderId, Long bookingId, String content) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("El mensaje no puede estar vacío");
        }
        if (content.length() > 1000) {
            throw new BadRequestException("El mensaje no puede superar los 1000 caracteres");
        }

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        User receiver;
        if (booking.getClient().getId().equals(senderId)) {
            receiver = booking.getProfessional().getUser();
        } else if (booking.getProfessional().getUser().getId().equals(senderId)) {
            receiver = booking.getClient();
        } else {
            throw new BadRequestException("No tienes permiso para enviar mensajes en esta reserva");
        }

        Message message = Message.builder()
            .booking(booking)
            .sender(sender)
            .receiver(receiver)
            .content(content.trim())
            .build();

        Message saved = messageRepository.save(message);

        notificationService.createNotification(
            receiver,
            "Nuevo mensaje de " + sender.getName(),
            content.length() > 50 ? content.substring(0, 50) + "..." : content,
            NotificationType.NEW_MESSAGE,
            bookingId
        );

        log.info("Mensaje enviado en reserva {} de usuario {} a {}", bookingId, senderId, receiver.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getByBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        boolean isParticipant = booking.getClient().getId().equals(userId)
            || booking.getProfessional().getUser().getId().equals(userId);
        if (!isParticipant) {
            throw new BadRequestException("No tienes acceso a esta conversación");
        }

        return messageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public int markAsRead(Long userId, Long bookingId) {
        return messageRepository.markBookingMessagesAsRead(bookingId, userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    private MessageResponse toResponse(Message m) {
        return new MessageResponse(
            m.getId(),
            m.getBooking().getId(),
            m.getSender().getId(),
            m.getSender().getName(),
            m.getReceiver().getId(),
            m.getReceiver().getName(),
            m.getContent(),
            m.getIsRead(),
            m.getCreatedAt()
        );
    }
}
