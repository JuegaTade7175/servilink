package com.example.demosass.service;

import com.example.demosass.domain.model.Category;
import com.example.demosass.domain.model.Service;
import com.example.demosass.domain.repository.CategoryRepository;
import com.example.demosass.domain.repository.ServiceRepository;
import com.example.demosass.dto.response.Responses.CategoryResponse;
import com.example.demosass.dto.response.Responses.ServiceResponse;
import com.example.demosass.exception.BadRequestException;
import com.example.demosass.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServiceRepository serviceRepository;

    public CategoryResponse create(com.example.demosass.dto.request.BookingRequest.CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("La categoría ya existe");
        }
        Category category = Category.builder()
            .name(request.name())
            .description(request.description())
            .iconUrl(request.iconUrl())
            .build();
        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse getById(Long id) {
        return toResponse(categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada")));
    }

    public List<ServiceResponse> getServicesByCategory(Long categoryId) {
        return serviceRepository.findByCategoryId(categoryId)
            .stream().map(this::serviceToResponse).toList();
    }

    public ServiceResponse createService(com.example.demosass.dto.request.BookingRequest.CreateServiceRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Service service = Service.builder()
            .name(request.name())
            .description(request.description())
            .referencePrice(request.referencePrice())
            .estimatedDurationHours(request.estimatedDurationHours())
            .category(category)
            .build();
        return serviceToResponse(serviceRepository.save(service));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.getIconUrl());
    }

    private ServiceResponse serviceToResponse(Service s) {
        return new ServiceResponse(
            s.getId(), s.getName(), s.getDescription(),
            s.getReferencePrice(), s.getEstimatedDurationHours(),
            s.getCategory() != null ? s.getCategory().getId() : null,
            s.getCategory() != null ? s.getCategory().getName() : null
        );
    }
}
