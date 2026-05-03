package com.example.demosass.controller;

import com.example.demosass.dto.request.ProfessionalRequest.*;
import com.example.demosass.dto.response.Responses.*;
import com.example.demosass.security.JwtUtil;
import com.example.demosass.service.ProfessionalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private final JwtUtil jwtUtil;

    @PostMapping("/profile")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<ProfessionalResponse> createProfile(
            @Valid @RequestBody CreateProfessionalRequest request,
            HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(professionalService.createProfile(userId, request));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<ProfessionalResponse> updateProfile(
            @RequestBody UpdateProfessionalRequest request,
            HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(professionalService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(professionalService.getById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<ProfessionalResponse> getMyProfile(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(professionalService.getByUserId(userId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ProfessionalResponse>> findNearby(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false, defaultValue = "10.0") Double radius,
            @RequestParam(required = false) Long categoryId) {
        NearbySearchRequest request = new NearbySearchRequest(lat, lon, radius, categoryId);
        return ResponseEntity.ok(professionalService.findNearby(request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProfessionalResponse>> search(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false, defaultValue = "10.0") Double radius,
            @RequestParam(required = false) Long categoryId) {

        if (lat != null && lon != null) {
            return findNearby(lat, lon, radius, categoryId);
        }
        if (categoryId != null) {
            return ResponseEntity.ok(professionalService.findByCategory(categoryId));
        }
        return ResponseEntity.badRequest().build();
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractUserId(token);
    }
}
