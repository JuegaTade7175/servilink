package com.example.demosass.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professionals")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Professional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String specialty;

    private String description;

    // Geolocalización con OpenStreetMap / Leaflet
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "coverage_radius_km")
    private Double coverageRadiusKm;

    @Column(name = "address")
    private String address;

    @Column(name = "base_rate")
    private BigDecimal baseRate;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "certifications")
    private String certifications;

    @ManyToMany
    @JoinTable(
        name = "professional_services",
        joinColumns = @JoinColumn(name = "professional_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    @Builder.Default
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Availability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "professional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}
