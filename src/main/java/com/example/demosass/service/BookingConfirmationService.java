package com.example.demosass.service;

import com.example.demosass.domain.enums.BookingStatus;
import com.example.demosass.domain.model.Booking;
import com.example.demosass.domain.model.BookingConfirmation;
import com.example.demosass.domain.model.BookingConfirmation.ConfirmationStatus;
import com.example.demosass.domain.repository.BookingConfirmationRepository;
import com.example.demosass.domain.repository.BookingRepository;
import com.example.demosass.dto.response.Responses.BookingConfirmationResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmationService {

    private final BookingConfirmationRepository confirmationRepository;
    private final BookingRepository bookingRepository;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_HOURS = 48;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public BookingConfirmationResponse generateConfirmation(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        return confirmationRepository.findByBookingId(bookingId)
            .map(existing -> toResponse(existing, booking))
            .orElseGet(() -> {
                String code = generateCode();

                BookingConfirmation confirmation = BookingConfirmation.builder()
                    .booking(booking)
                    .confirmationCode(code)
                    .status(ConfirmationStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusHours(EXPIRATION_HOURS))
                    .build();

                log.info("Código de confirmación generado para reserva {}: {}",
                    bookingId, code);

                return toResponse(confirmationRepository.save(confirmation), booking);
            });
    }

    @Transactional
    public BookingConfirmationResponse confirmWithCode(Long professionalId, String code) {
        BookingConfirmation confirmation = confirmationRepository
            .findByConfirmationCodeAndStatus(code, ConfirmationStatus.PENDING)
            .orElseThrow(() -> new BadRequestException(
                "Código inválido o ya utilizado. Verifica el código en tu dashboard."
            ));

        if (LocalDateTime.now().isAfter(confirmation.getExpiresAt())) {
            confirmation.setStatus(ConfirmationStatus.EXPIRED);
            confirmationRepository.save(confirmation);
            throw new BadRequestException("El código ha expirado. Solicita uno nuevo al cliente.");
        }

        Booking booking = confirmation.getBooking();
        if (!booking.getProfessional().getId().equals(professionalId)) {
            throw new BadRequestException("Este código no corresponde a ninguna de tus reservas.");
        }

        confirmation.setStatus(ConfirmationStatus.CONFIRMED);
        confirmation.setConfirmedAt(LocalDateTime.now());
        confirmationRepository.save(confirmation);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        log.info("Reserva {} confirmada por profesional {} con código {}",
            booking.getId(), professionalId, code);

        return toResponse(confirmation, booking);
    }

    @Transactional(readOnly = true)
    public BookingConfirmationResponse getConfirmationStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        BookingConfirmation confirmation = confirmationRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No se encontró confirmación para esta reserva"));

        return toResponse(confirmation, booking);
    }

    @Transactional
    public void cancelConfirmation(Long bookingId) {
        BookingConfirmation confirmation = confirmationRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Confirmación no encontrada"));

        if (confirmation.getStatus() != ConfirmationStatus.PENDING) {
            throw new BadRequestException("Solo se pueden cancelar confirmaciones pendientes");
        }

        confirmation.setStatus(ConfirmationStatus.CANCELLED);
        confirmationRepository.save(confirmation);

        Booking booking = confirmation.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void expireOldConfirmations() {
        int expired = confirmationRepository.expireOldConfirmations(LocalDateTime.now());
        if (expired > 0) {
            log.info("Se expiraron {} confirmaciones vencidas", expired);
        }
    }

    private String generateCode() {
        int code = 100_000 + random.nextInt(900_000);
        return String.valueOf(code);
    }

    private BookingConfirmationResponse toResponse(BookingConfirmation c, Booking b) {
        return new BookingConfirmationResponse(
            c.getId(),
            b.getId(),
            b.getClient().getName(),
            b.getProfessional().getUser().getName(),
            b.getService().getName(),
            b.getScheduledAt(),
            c.getConfirmationCode(),
            c.getStatus(),
            c.getConfirmedAt(),
            c.getExpiresAt(),
            c.getCreatedAt()
        );
    }
}
