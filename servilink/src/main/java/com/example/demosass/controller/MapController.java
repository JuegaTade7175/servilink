package com.example.demosass.controller;

import com.example.demosass.dto.response.Responses.GeoPointResponse;
import com.example.demosass.service.GeoService;
import com.example.demosass.service.ProfessionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller dedicado para la integración con Leaflet + OpenStreetMap.
 * Todos los endpoints son públicos para que el mapa cargue sin auth.
 */
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final ProfessionalService professionalService;
    private final GeoService geoService;

    /**
     * GeoPoints para los marcadores del mapa Leaflet.
     * Responde con lat, lon y metadata de cada profesional.
     */
    @GetMapping("/professionals")
    public ResponseEntity<List<GeoPointResponse>> getProfessionalsForMap(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false, defaultValue = "10.0") Double radius,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(
            professionalService.getGeoPoints(lat, lon, radius, categoryId)
        );
    }

    /**
     * Geocodifica una dirección usando OpenStreetMap Nominatim.
     * El frontend usa esto para centrar el mapa en la dirección del usuario.
     */
    @GetMapping("/geocode")
    public ResponseEntity<?> geocode(@RequestParam String address) {
        GeoService.GeocodingResult result = geoService.geocodeAddress(address);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "latitude", result.latitude(),
            "longitude", result.longitude(),
            "address", result.formattedAddress()
        ));
    }

    /**
     * Reverse geocoding: convierte coordenadas en dirección legible.
     * Útil cuando el usuario hace click en el mapa Leaflet.
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lon) {
        String address = geoService.reverseGeocode(lat, lon);
        if (address == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("address", address));
    }

    /**
     * Calcula distancia entre dos puntos (Haversine).
     * Usado por el frontend para mostrar "X km de ti".
     */
    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam Double lat1, @RequestParam Double lon1,
            @RequestParam Double lat2, @RequestParam Double lon2) {
        double distanceKm = geoService.calculateDistance(lat1, lon1, lat2, lon2);
        return ResponseEntity.ok(Map.of(
            "distanceKm", Math.round(distanceKm * 100.0) / 100.0,
            "distanceM", Math.round(distanceKm * 1000)
        ));
    }
}
