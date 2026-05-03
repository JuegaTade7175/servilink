package com.example.demosass.dto.response;

import com.example.demosass.domain.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Responses {

    public record AuthResponse(
        String token,
        String type,
        Long userId,
        String name,
        String email,
        Role role
    ) {
        public static AuthResponse of(String token, Long userId, String name, String email, Role role) {
            return new AuthResponse(token, "Bearer", userId, name, email, role);
        }
    }

    public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String profilePictureUrl,
        Role role,
        LocalDateTime createdAt
    ) {}

    public record ProfessionalResponse(
        Long id,
        Long userId,
        String userName,
        String userEmail,
        String userPhone,
        String profilePictureUrl,
        String specialty,
        String description,
        Double latitude,
        Double longitude,
        String address,
        Double coverageRadiusKm,
        BigDecimal baseRate,
        Boolean isVerified,
        Double averageRating,
        Integer totalReviews,
        String certifications,
        Double distanceKm,
        List<ServiceResponse> services
    ) {}

    public record ServiceResponse(
        Long id,
        String name,
        String description,
        BigDecimal referencePrice,
        Integer estimatedDurationHours,
        Long categoryId,
        String categoryName
    ) {}

    public record CategoryResponse(
        Long id,
        String name,
        String description,
        String iconUrl
    ) {}

    public record AvailabilityResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Boolean isAvailable
    ) {}

    public record BookingResponse(
        Long id,
        Long clientId,
        String clientName,
        Long professionalId,
        String professionalName,
        Long serviceId,
        String serviceName,
        LocalDateTime scheduledAt,
        String address,
        String description,
        BookingStatus status,
        LocalDateTime createdAt
    ) {}

    public record PaymentResponse(
        Long id,
        Long bookingId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String transactionId,
        LocalDateTime paidAt,
        LocalDateTime createdAt
    ) {}

    public record ReviewResponse(
        Long id,
        Long bookingId,
        Long professionalId,
        String professionalName,
        Long clientId,
        String clientName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
    ) {}

    // Respuesta para mapa Leaflet / OpenStreetMap
    public record GeoPointResponse(
        Long professionalId,
        String name,
        String specialty,
        Double latitude,
        Double longitude,
        Double averageRating,
        BigDecimal baseRate,
        Double distanceKm,
        Boolean isVerified
    ) {}

    public record ErrorResponse(
        String message,
        String details,
        int status
    ) {}
}
