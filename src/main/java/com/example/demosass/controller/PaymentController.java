package com.example.demosass.controller;

import com.example.demosass.domain.enums.PaymentMethod;
import com.example.demosass.dto.response.Responses.*;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<PaymentResponse> process(@RequestBody Map<String, Object> body) {
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        PaymentMethod method = PaymentMethod.valueOf(body.get("method").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.processPayment(bookingId, amount, method));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getByBookingId(bookingId));
    }
}

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        Long clientId = extractUserId(request);
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String comment = (String) body.get("comment");
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reviewService.create(clientId, bookingId, rating, comment));
    }

    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<List<ReviewResponse>> getByProfessional(@PathVariable Long professionalId) {
        return ResponseEntity.ok(reviewService.getByProfessional(professionalId));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            categoryService.create(body.get("name"), body.get("description"), body.get("iconUrl"))
        );
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponse>> getServices(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getServicesByCategory(id));
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ServiceResponse> addService(
            @PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createService(
            body.get("name").toString(),
            body.get("description") != null ? body.get("description").toString() : null,
            body.get("referencePrice") != null ? new BigDecimal(body.get("referencePrice").toString()) : null,
            body.get("estimatedDurationHours") != null ? Integer.valueOf(body.get("estimatedDurationHours").toString()) : null,
            id
        ));
    }
}
