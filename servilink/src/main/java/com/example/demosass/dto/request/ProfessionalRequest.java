package com.example.demosass.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProfessionalRequest {

    public record CreateProfessionalRequest(
        @NotBlank(message = "La especialidad es obligatoria")
        String specialty,

        String description,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        Double latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        Double longitude,

        String address,

        @DecimalMin(value = "0.5")
        Double coverageRadiusKm,

        @NotNull(message = "La tarifa base es obligatoria")
        @DecimalMin(value = "0.0", message = "La tarifa debe ser positiva")
        BigDecimal baseRate,

        String certifications
    ) {}

    public record UpdateProfessionalRequest(
        String specialty,
        String description,
        Double latitude,
        Double longitude,
        String address,
        Double coverageRadiusKm,
        BigDecimal baseRate,
        String certifications
    ) {}

    public record NearbySearchRequest(
        @NotNull Double latitude,
        @NotNull Double longitude,
        Double radiusKm,
        Long categoryId
    ) {}
}
