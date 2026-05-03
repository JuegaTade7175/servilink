package com.example.demosass.dto.request;

import com.example.demosass.domain.enums.DayOfWeek;
import com.example.demosass.domain.enums.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BookingRequest {

    public record CreateBookingRequest(
        @NotNull(message = "El profesional es obligatorio")
        Long professionalId,

        @NotNull(message = "El servicio es obligatorio")
        Long serviceId,

        @NotNull(message = "La fecha y hora son obligatorias")
        @Future(message = "La reserva debe ser en el futuro")
        LocalDateTime scheduledAt,

        @NotBlank(message = "La dirección es obligatoria")
        String address,

        Double clientLatitude,
        Double clientLongitude,

        String description
    ) {}

    public record UpdateBookingStatusRequest(
        @NotBlank String status
    ) {}
}

class PaymentRequest {
    public record CreatePaymentRequest(
        @NotNull Long bookingId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentMethod method
    ) {}
}

class ReviewRequest {
    public record CreateReviewRequest(
        @NotNull Long bookingId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
    ) {}
}

class CategoryRequest {
    public record CreateCategoryRequest(
        @NotBlank String name,
        String description,
        String iconUrl
    ) {}
}

class ServiceRequest {
    public record CreateServiceRequest(
        @NotBlank String name,
        String description,
        BigDecimal referencePrice,
        Integer estimatedDurationHours,
        @NotNull Long categoryId
    ) {}
}

class AvailabilityRequest {
    public record CreateAvailabilityRequest(
        @NotNull DayOfWeek dayOfWeek,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime
    ) {}
}
