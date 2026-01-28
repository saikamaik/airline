package com.example.airline.controller.admin;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.service.tour.TourService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/admin/tours")
public class AdminTourController {
    private static final Logger logger = LoggerFactory.getLogger(AdminTourController.class);
    
    private final TourService tourService;

    public AdminTourController(TourService tourService) {
        this.tourService = tourService;
    }

    @GetMapping
    public ResponseEntity<Page<TourDto>> getAllTours(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.debug("Запрос туров: страница={}, размер={}, направление={}", page, size, destination);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TourDto> tours = tourService.findWithFilters(destination, minPrice, maxPrice, pageable);
        logger.debug("Найдено туров: {}", tours.getTotalElements());
        return ResponseEntity.ok(tours);
    }

    @PostMapping
    public ResponseEntity<TourDto> createTour(@Valid @RequestBody TourDto tourDto) {
        try {
            logger.info("Creating tour: {}", tourDto.getName());
            TourDto created = tourService.createTour(tourDto);
            logger.info("Tour created successfully with ID: {}", created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create tour: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Unexpected error while creating tour", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDto> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourDto tourDto) {
        try {
            logger.info("Updating tour with ID: {}", id);
            TourDto updated = tourService.updateTour(id, tourDto);
            logger.info("Tour updated successfully: {}", id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Tour not found or invalid data for ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error while updating tour with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        try {
            logger.info("Deleting tour with ID: {}", id);
            tourService.deleteTour(id);
            logger.info("Tour deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Tour not found for deletion, ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Unexpected error while deleting tour with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

