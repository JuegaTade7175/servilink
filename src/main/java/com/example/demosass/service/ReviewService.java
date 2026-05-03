package com.example.demosass.service;

import com.example.demosass.domain.enums.BookingStatus;
import com.example.demosass.domain.model.*;
import com.example.demosass.domain.repository.*;
import com.example.demosass.dto.response.Responses.ReviewResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;

    @Transactional
    public ReviewResponse create(Long clientId, Long bookingId, Integer rating, String comment) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        if (!booking.getClient().getId().equals(clientId)) {
            throw new BadRequestException("No puedes reseñar una reserva que no es tuya");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BadRequestException("Solo puedes reseñar servicios completados");
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new BadRequestException("Esta reserva ya tiene una reseña");
        }

        User client = userRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Review review = Review.builder()
            .booking(booking)
            .professional(booking.getProfessional())
            .client(client)
            .rating(rating)
            .comment(comment)
            .build();

        reviewRepository.save(review);

        Professional professional = booking.getProfessional();
        Double avg = reviewRepository.calculateAverageRating(professional.getId());
        professional.setAverageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        professional.setTotalReviews(reviewRepository.findByProfessionalId(professional.getId()).size());
        professionalRepository.save(professional);

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getByProfessional(Long professionalId) {
        return reviewRepository.findByProfessionalId(professionalId)
            .stream().map(this::toResponse).toList();
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
            r.getId(),
            r.getBooking().getId(),
            r.getProfessional().getId(),
            r.getProfessional().getUser().getName(),
            r.getClient().getId(),
            r.getClient().getName(),
            r.getRating(),
            r.getComment(),
            r.getCreatedAt()
        );
    }
}
