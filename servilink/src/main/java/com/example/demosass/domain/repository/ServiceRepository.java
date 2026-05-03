package com.example.demosass.domain.repository;

import com.example.demosass.domain.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findByCategoryId(Long categoryId);
}
