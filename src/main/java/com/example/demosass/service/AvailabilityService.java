package com.example.demosass.service;

import com.example.demosass.domain.enums.DayOfWeek;
import com.example.demosass.domain.model.Availability;
import com.example.demosass.domain.model.Professional;
import com.example.demosass.domain.repository.AvailabilityRepository;
import com.example.demosass.domain.repository.ProfessionalRepository;
import com.example.demosass.dto.response.Responses.AvailabilityResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final ProfessionalRepository professionalRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByProfessional(Long professionalId) {
        return availabilityRepository.findByProfessionalId(professionalId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByProfessionalAndDay(Long professionalId, DayOfWeek day) {
        return availabilityRepository.findByProfessionalIdAndDayOfWeek(professionalId, day)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AvailabilityResponse create(Long userId, DayOfWeek dayOfWeek,
                                        LocalTime startTime, LocalTime endTime) {
        Professional professional = professionalRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));

        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new BadRequestException("La hora de inicio debe ser anterior a la hora de fin");
        }

        Availability availability = Availability.builder()
            .professional(professional)
            .dayOfWeek(dayOfWeek)
            .startTime(startTime)
            .endTime(endTime)
            .isAvailable(true)
            .build();

        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public AvailabilityResponse update(Long userId, Long availabilityId,
                                        LocalTime startTime, LocalTime endTime, Boolean isAvailable) {
        Availability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Disponibilidad no encontrada"));

        Professional professional = professionalRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));

        if (!availability.getProfessional().getId().equals(professional.getId())) {
            throw new BadRequestException("No tienes permiso para modificar esta disponibilidad");
        }

        if (startTime != null && endTime != null) {
            if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
                throw new BadRequestException("La hora de inicio debe ser anterior a la hora de fin");
            }
            availability.setStartTime(startTime);
            availability.setEndTime(endTime);
        }
        if (isAvailable != null) {
            availability.setIsAvailable(isAvailable);
        }

        return toResponse(availabilityRepository.save(availability));
    }

    @Transactional
    public void delete(Long userId, Long availabilityId) {
        Availability availability = availabilityRepository.findById(availabilityId)
            .orElseThrow(() -> new ResourceNotFoundException("Disponibilidad no encontrada"));

        Professional professional = professionalRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil profesional no encontrado"));

        if (!availability.getProfessional().getId().equals(professional.getId())) {
            throw new BadRequestException("No tienes permiso para eliminar esta disponibilidad");
        }

        availabilityRepository.delete(availability);
    }

    private AvailabilityResponse toResponse(Availability a) {
        return new AvailabilityResponse(
            a.getId(),
            a.getDayOfWeek(),
            a.getStartTime(),
            a.getEndTime(),
            a.getIsAvailable()
        );
    }
}
