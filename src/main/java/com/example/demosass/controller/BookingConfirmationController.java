package com.example.demosass.controller;

import com.example.demosass.dto.response.Responses.BookingConfirmationResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.BookingConfirmationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de confirmación de citas — implementación interna (sin APIs externas).
 *
 * Flujo completo:
 *   POST /api/confirmations/booking/{id}/generate  → cliente o sistema genera el código
 *   POST /api/confirmations/confirm                → profesional ingresa el código
 *   GET  /api/confirmations/booking/{id}           → ver estado
 *   DELETE /api/confirmations/booking/{id}         → cancelar
 */
@RestController
@RequestMapping("/api/confirmations")
@RequiredArgsConstructor
public class BookingConfirmationController {

    private final BookingConfirmationService confirmationService;
    private final JwtUtil jwtUtil;

    /**
     * Genera el código de confirmación para una reserva.
     * Lo llama el sistema automáticamente al crear la reserva,
     * o el cliente manualmente si necesita regenerarlo.
     */
    @PostMapping("/booking/{bookingId}/generate")
    public ResponseEntity<BookingConfirmationResponse> generate(
            @PathVariable Long bookingId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(confirmationService.generateConfirmation(bookingId));
    }

    /**
     * El profesional ingresa el código para confirmar la cita.
     * Request body: { "code": "123456" }
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<BookingConfirmationResponse> confirm(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long professionalId = extractUserId(request);
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(confirmationService.confirmWithCode(professionalId, code));
    }

    /**
     * Ver el estado de confirmación de una reserva.
     * El profesional ve su código aquí en el dashboard.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<BookingConfirmationResponse> getStatus(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(confirmationService.getConfirmationStatus(bookingId));
    }

    /**
     * Cancelar la confirmación (y la reserva).
     */
    @DeleteMapping("/booking/{bookingId}")
    public ResponseEntity<Void> cancel(@PathVariable Long bookingId) {
        confirmationService.cancelConfirmation(bookingId);
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
