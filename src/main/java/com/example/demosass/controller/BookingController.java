package com.example.demosass.controller;

import com.example.demosass.dto.request.BookingRequest.CreateBookingRequest;
import com.example.demosass.dto.response.Responses.BookingResponse;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody CreateBookingRequest request,
            HttpServletRequest httpRequest) {
        Long clientId = extractUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(bookingService.create(clientId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(HttpServletRequest request) {
        Long clientId = extractUserId(request);
        return ResponseEntity.ok(bookingService.getByClient(clientId));
    }

    @GetMapping("/professional")
    public ResponseEntity<List<BookingResponse>> getProfessionalBookings(
            @RequestParam Long professionalId) {
        return ResponseEntity.ok(bookingService.getByProfessional(professionalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(bookingService.updateStatus(id, body.get("status"), userId));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
