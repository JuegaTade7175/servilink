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
        @NotBlank(message = "El estado es obligatorio")
        String status
    ) {}

    public record CreatePaymentRequest(
        @NotNull(message = "La reserva es obligatoria")
        Long bookingId,
        
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser al menos 0.01")
        BigDecimal amount,
        
        @NotNull(message = "El método de pago es obligatorio")
        PaymentMethod method,
        
        String transactionId
    ) {}

    public record CreateReviewRequest(
        @NotNull(message = "La reserva es obligatoria")
        Long bookingId,
        
        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 1, message = "La calificación mínima es 1")
        @Max(value = 5, message = "La calificación máxima es 5")
        Integer rating,
        
        String comment
    ) {}

    public record CreateCategoryRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        
        String description,
        
        String iconUrl
    ) {}

    public record CreateServiceRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String name,
        
        String description,
        
        @DecimalMin(value = "0.0", message = "El precio de referencia no puede ser negativo")
        BigDecimal referencePrice,
        
        @Min(value = 0, message = "La duración estimada no puede ser negativa")
        Integer estimatedDurationHours,
        
        @NotNull(message = "La categoría es obligatoria")
        Long categoryId
    ) {}

    public record CreateAvailabilityRequest(
        @NotNull(message = "El día de la semana es obligatorio")
        DayOfWeek dayOfWeek,
        
        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,
        
        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime,
        
        Boolean isAvailable
    ) {}
}
