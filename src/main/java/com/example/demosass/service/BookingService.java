package com.example.demosass.service;

import com.example.demosass.domain.enums.BookingStatus;
import com.example.demosass.domain.model.*;
import com.example.demosass.domain.repository.*;
import com.example.demosass.dto.request.BookingRequest.CreateBookingRequest;
import com.example.demosass.dto.response.Responses.BookingResponse;
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
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final ServiceRepository serviceRepository;
    private final BookingConfirmationService confirmationService;

    @Transactional
    public BookingResponse create(Long clientId, CreateBookingRequest request) {
        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Professional professional = professionalRepository.findById(request.professionalId())
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));

        com.example.demosass.domain.model.Service service =
            serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        Booking booking = Booking.builder()
            .client(client)
            .professional(professional)
            .service(service)
            .scheduledAt(request.scheduledAt())
            .address(request.address())
            .clientLatitude(request.clientLatitude())
            .clientLongitude(request.clientLongitude())
            .description(request.description())
            .status(BookingStatus.PENDING)
            .build();

        Booking saved = bookingRepository.save(booking);

        try {
            confirmationService.generateConfirmation(saved.getId());
            log.info("Código de confirmación generado para reserva {}", saved.getId());
        } catch (Exception e) {
            log.warn("No se pudo generar confirmación para reserva {}: {}", saved.getId(), e.getMessage());
        }

        return toResponse(saved);
    }

    @Transactional
    public BookingResponse updateStatus(Long bookingId, String newStatus, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        BookingStatus status;
        try {
            status = BookingStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + newStatus +
                ". Valores válidos: PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED");
        }

        booking.setStatus(status);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByClient(Long clientId) {
        return bookingRepository.findByClientId(clientId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByProfessional(Long professionalId) {
        return bookingRepository.findByProfessionalId(professionalId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getByClientAndStatus(Long clientId, String status) {
        try {
            BookingStatus bs = BookingStatus.valueOf(status.toUpperCase());
            return bookingRepository.findByClientIdAndStatus(clientId, bs)
                .stream().map(this::toResponse).toList();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + status);
        }
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return toResponse(bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada")));
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(
            b.getId(),
            b.getClient().getId(),
            b.getClient().getName(),
            b.getProfessional().getId(),
            b.getProfessional().getUser().getName(),
            b.getService().getId(),
            b.getService().getName(),
            b.getScheduledAt(),
            b.getAddress(),
            b.getDescription(),
            b.getStatus(),
            b.getCreatedAt()
        );
    }
}
