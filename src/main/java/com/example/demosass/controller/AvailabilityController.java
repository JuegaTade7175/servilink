package com.example.demosass.controller;

import com.example.demosass.domain.enums.DayOfWeek;
import com.example.demosass.dto.response.Responses.AvailabilityResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.AvailabilityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final JwtUtil jwtUtil;

    /**
     * Obtener disponibilidad de un profesional (público).
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<AvailabilityResponse>> getByProfessional(
            @PathVariable Long professionalId) {
        return ResponseEntity.ok(availabilityService.getByProfessional(professionalId));
    }

    /**
     * Obtener disponibilidad por día.
     * Ejemplo: GET /api/availability/professional/1?day=MONDAY
     */
    @GetMapping("/professional/{professionalId}/day")
    public ResponseEntity<List<AvailabilityResponse>> getByDay(
            @PathVariable Long professionalId,
            @RequestParam DayOfWeek day) {
        return ResponseEntity.ok(availabilityService.getByProfessionalAndDay(professionalId, day));
    }

    /**
     * Crear horario (solo PROFESSIONAL).
     * Body: { "dayOfWeek": "MONDAY", "startTime": "08:00", "endTime": "18:00" }
     */
    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<AvailabilityResponse> create(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        DayOfWeek day = DayOfWeek.valueOf(body.get("dayOfWeek").toUpperCase());
        LocalTime start = LocalTime.parse(body.get("startTime"));
        LocalTime end = LocalTime.parse(body.get("endTime"));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(availabilityService.create(userId, day, start, end));
    }

    /**
     * Actualizar horario (solo PROFESSIONAL dueño).
     * Body: { "startTime": "09:00", "endTime": "17:00", "isAvailable": true }
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<AvailabilityResponse> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        LocalTime start = body.containsKey("startTime")
            ? LocalTime.parse(body.get("startTime").toString()) : null;
        LocalTime end = body.containsKey("endTime")
            ? LocalTime.parse(body.get("endTime").toString()) : null;
        Boolean isAvailable = body.containsKey("isAvailable")
            ? Boolean.valueOf(body.get("isAvailable").toString()) : null;
        return ResponseEntity.ok(availabilityService.update(userId, id, start, end, isAvailable));
    }

    /**
     * Eliminar horario (solo PROFESSIONAL dueño).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = extractUserId(request);
        availabilityService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
