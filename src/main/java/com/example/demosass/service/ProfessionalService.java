package com.example.demosass.service;

import com.example.demosass.domain.model.Professional;
import com.example.demosass.domain.model.User;
import com.example.demosass.domain.repository.ProfessionalRepository;
import com.example.demosass.domain.repository.UserRepository;
import com.example.demosass.dto.request.ProfessionalRequest.*;
import com.example.demosass.dto.response.Responses.*;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;
    private final GeoService geoService;

    private static final double DEFAULT_RADIUS_KM = 10.0;

    @Transactional
    public ProfessionalResponse createProfile(Long userId, CreateProfessionalRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (professionalRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("El usuario ya tiene un perfil profesional");
        }

        Professional professional = Professional.builder()
            .user(user)
            .specialty(request.specialty())
            .description(request.description())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .address(request.address())
            .coverageRadiusKm(request.coverageRadiusKm() != null ? request.coverageRadiusKm() : DEFAULT_RADIUS_KM)
            .baseRate(request.baseRate())
            .certifications(request.certifications())
            .build();

        // Si se proporciona dirección pero no coordenadas, geocodificar con OpenStreetMap
        if (request.address() != null && request.latitude() == null) {
            GeoService.GeocodingResult geo = geoService.geocodeAddress(request.address());
            if (geo != null) {
                professional.setLatitude(geo.latitude());
                professional.setLongitude(geo.longitude());
            }
        }

        return toResponse(professionalRepository.save(professional), null);
    }

    @Transactional
    public ProfessionalResponse updateProfile(Long userId, UpdateProfessionalRequest request) {
        Professional professional = professionalRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));

        if (request.specialty() != null) professional.setSpecialty(request.specialty());
        if (request.description() != null) professional.setDescription(request.description());
        if (request.latitude() != null) professional.setLatitude(request.latitude());
        if (request.longitude() != null) professional.setLongitude(request.longitude());
        if (request.address() != null) professional.setAddress(request.address());
        if (request.coverageRadiusKm() != null) professional.setCoverageRadiusKm(request.coverageRadiusKm());
        if (request.baseRate() != null) professional.setBaseRate(request.baseRate());
        if (request.certifications() != null) professional.setCertifications(request.certifications());

        return toResponse(professionalRepository.save(professional), null);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse getById(Long id) {
        Professional p = professionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));
        return toResponse(p, null);
    }

    @Transactional(readOnly = true)
    public ProfessionalResponse getByUserId(Long userId) {
        Professional p = professionalRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));
        return toResponse(p, null);
    }

    /**
     * Búsqueda por cercanía usando Haversine en DB + distancia calculada.
     * Optimizado para Leaflet: retorna coordenadas listas para el mapa.
     */
    @Transactional(readOnly = true)
    public List<ProfessionalResponse> findNearby(NearbySearchRequest request) {
        double radius = request.radiusKm() != null ? request.radiusKm() : DEFAULT_RADIUS_KM;
        List<Professional> professionals;

        if (request.categoryId() != null) {
            professionals = professionalRepository.findByCategoryAndLocation(
                request.categoryId(), request.latitude(), request.longitude(), radius
            );
        } else {
            professionals = professionalRepository.findNearbyProfessionals(
                request.latitude(), request.longitude(), radius
            );
        }

        return professionals.stream()
            .map(p -> toResponse(p, geoService.calculateDistance(
                request.latitude(), request.longitude(),
                p.getLatitude(), p.getLongitude()
            )))
            .toList();
    }

    /**
     * Retorna GeoPoints para el mapa Leaflet — endpoint especial para el frontend.
     */
    @Transactional(readOnly = true)
    public List<GeoPointResponse> getGeoPoints(Double lat, Double lon, Double radius, Long categoryId) {
        NearbySearchRequest req = new NearbySearchRequest(lat, lon, radius, categoryId);
        return findNearby(req).stream()
            .map(p -> new GeoPointResponse(
                p.id(), p.userName(), p.specialty(),
                p.latitude(), p.longitude(),
                p.averageRating(), p.baseRate(),
                p.distanceKm(), p.isVerified()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfessionalResponse> findByCategory(Long categoryId) {
        return professionalRepository.findByCategoryId(categoryId)
            .stream().map(p -> toResponse(p, null)).toList();
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private ProfessionalResponse toResponse(Professional p, Double distanceKm) {
        List<ServiceResponse> services = p.getServices().stream()
            .map(s -> new ServiceResponse(
                s.getId(), s.getName(), s.getDescription(),
                s.getReferencePrice(), s.getEstimatedDurationHours(),
                s.getCategory() != null ? s.getCategory().getId() : null,
                s.getCategory() != null ? s.getCategory().getName() : null
            )).toList();

        return new ProfessionalResponse(
            p.getId(),
            p.getUser().getId(),
            p.getUser().getName(),
            p.getUser().getEmail(),
            p.getUser().getPhone(),
            p.getUser().getProfilePictureUrl(),
            p.getSpecialty(),
            p.getDescription(),
            p.getLatitude(),
            p.getLongitude(),
            p.getAddress(),
            p.getCoverageRadiusKm(),
            p.getBaseRate(),
            p.getIsVerified(),
            p.getAverageRating(),
            p.getTotalReviews(),
            p.getCertifications(),
            distanceKm,
            services
        );
    }
}
