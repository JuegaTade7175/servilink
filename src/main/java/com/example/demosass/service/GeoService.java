package com.example.demosass.service;

import com.example.demosass.dto.response.Responses.GeoPointResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Servicio de geolocalización usando OpenStreetMap Nominatim (gratuito, open source).
 * Reemplaza Google Maps API — sugerencia de los profesores del curso.
 * Docs: https://nominatim.org/release-docs/develop/api/Search/
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeoService {

    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";
    private static final double EARTH_RADIUS_KM = 6371.0;

    private final RestTemplate restTemplate;

    /**
     * Geocoding: convierte una dirección en coordenadas lat/lon.
     * Usando Nominatim (OpenStreetMap) — gratuito y open source.
     */
    public GeocodingResult geocodeAddress(String address) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_BASE_URL + "/search")
                .queryParam("q", address + ", Lima, Perú")
                .queryParam("format", "json")
                .queryParam("limit", "1")
                .queryParam("countrycodes", "pe")
                .toUriString();

            NominatimResult[] results = restTemplate.getForObject(url, NominatimResult[].class);

            if (results != null && results.length > 0) {
                NominatimResult result = results[0];
                return new GeocodingResult(
                    Double.parseDouble(result.lat()),
                    Double.parseDouble(result.lon()),
                    result.displayName()
                );
            }
        } catch (Exception e) {
            log.warn("Error geocodificando dirección '{}': {}", address, e.getMessage());
        }
        return null;
    }

    /**
     * Reverse geocoding: convierte coordenadas en dirección legible.
     */
    public String reverseGeocode(Double latitude, Double longitude) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_BASE_URL + "/reverse")
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("format", "json")
                .toUriString();

            NominatimReverseResult result = restTemplate.getForObject(url, NominatimReverseResult.class);
            if (result != null && result.displayName() != null) {
                return result.displayName();
            }
        } catch (Exception e) {
            log.warn("Error en reverse geocoding ({}, {}): {}", latitude, longitude, e.getMessage());
        }
        return null;
    }

    /**
     * Fórmula Haversine: calcula distancia entre dos coordenadas.
     * Usada internamente y expuesta para el frontend (Leaflet).
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Retorna una lista de puntos para renderizar en Leaflet (frontend).
     * Formato compatible con GeoJSON simplificado.
     */
    public List<double[]> buildLeafletMarkers(List<GeoPointResponse> points) {
        return points.stream()
            .filter(p -> p.latitude() != null && p.longitude() != null)
            .map(p -> new double[]{p.latitude(), p.longitude()})
            .toList();
    }

    // ─── Records para deserializar la respuesta de Nominatim ───────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NominatimResult(
        String lat,
        String lon,
        @JsonProperty("display_name") String displayName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NominatimReverseResult(
        @JsonProperty("display_name") String displayName
    ) {}

    public record GeocodingResult(
        Double latitude,
        Double longitude,
        String formattedAddress
    ) {}
}
