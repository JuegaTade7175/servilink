package com.example.demosass.service;

import com.example.demosass.domain.enums.BookingStatus;
import com.example.demosass.domain.enums.PaymentMethod;
import com.example.demosass.domain.enums.PaymentStatus;
import com.example.demosass.domain.model.Booking;
import com.example.demosass.domain.model.Payment;
import com.example.demosass.domain.repository.BookingRepository;
import com.example.demosass.domain.repository.PaymentRepository;
import com.example.demosass.dto.response.Responses.PaymentResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    /**
     * Procesa un pago para una reserva.
     * En MVP simula el proceso — en producción integrar MercadoPago/Stripe.
     */
    @Transactional
    public PaymentResponse processPayment(com.example.demosass.dto.request.BookingRequest.CreatePaymentRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (paymentRepository.findByBookingId(request.bookingId()).isPresent()) {
            throw new BadRequestException("Esta reserva ya tiene un pago asociado");
        }

        if (booking.getStatus() == com.example.demosass.domain.enums.BookingStatus.CANCELLED) {
            throw new BadRequestException("No se puede pagar una reserva cancelada");
        }

        // Simulación del procesamiento de pago (MVP)
        String transactionId = request.transactionId() != null ? request.transactionId() : 
            "SL-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("Procesando pago {} con método {} para reserva {}", transactionId, request.method(), request.bookingId());

        Payment payment = Payment.builder()
            .booking(booking)
            .amount(request.amount())
            .method(request.method())
            .status(PaymentStatus.COMPLETED)
            .transactionId(transactionId)
            .paidAt(LocalDateTime.now())
            .build();

        // Actualizar estado de la reserva a CONFIRMED
        booking.setStatus(com.example.demosass.domain.enums.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return toResponse(paymentRepository.save(payment));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByBookingId(Long bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para la reserva"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
            p.getId(),
            p.getBooking().getId(),
            p.getAmount(),
            p.getMethod(),
            p.getStatus(),
            p.getTransactionId(),
            p.getPaidAt(),
            p.getCreatedAt()
        );
    }
}
