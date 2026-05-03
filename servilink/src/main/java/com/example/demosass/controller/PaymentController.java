package com.example.demosass.controller;

import com.example.demosass.dto.request.BookingRequest.*;
import com.example.demosass.dto.response.Responses.*;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> process(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(paymentService.processPayment(request));
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
            @Valid @RequestBody CreateReviewRequest request,
            HttpServletRequest httpRequest) {
        Long clientId = extractUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(reviewService.create(clientId, request));
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
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            categoryService.create(request)
        );
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceResponse>> getServices(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getServicesByCategory(id));
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ServiceResponse> addService(
            @PathVariable Long id, @Valid @RequestBody CreateServiceRequest request) {
        // En este diseño, el id de categoría viene en el path o en el body. 
        // Si viene en ambos, priorizamos el path o validamos que coincidan.
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createService(request));
    }
}
