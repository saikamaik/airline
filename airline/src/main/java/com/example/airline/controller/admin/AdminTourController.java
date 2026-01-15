package com.example.airline.controller.admin;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.service.tour.TourService;
import jakarta.validation.Valid;
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
        System.out.println("=== AdminTourController: Запрос туров, страница: " + page + ", размер: " + size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TourDto> tours = tourService.findWithFilters(destination, minPrice, maxPrice, pageable);
        System.out.println("=== AdminTourController: Найдено туров: " + tours.getTotalElements());
        return ResponseEntity.ok(tours);
    }

    @PostMapping
    public ResponseEntity<TourDto> createTour(@Valid @RequestBody TourDto tourDto) {
        try {
            TourDto created = tourService.createTour(tourDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourDto> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourDto tourDto) {
        try {
            TourDto updated = tourService.updateTour(id, tourDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable Long id) {
        try {
            tourService.deleteTour(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

