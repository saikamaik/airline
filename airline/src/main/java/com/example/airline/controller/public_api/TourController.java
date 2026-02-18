package com.example.airline.controller.public_api;

import com.example.airline.dto.request.ClientRequestDto;
import com.example.airline.dto.flight.FlightDto;
import com.example.airline.dto.tour.TourDto;
import com.example.airline.entity.flight.Flight;
import com.example.airline.mapper.tour.TourMapper;
import com.example.airline.service.request.ClientRequestService;
import com.example.airline.service.tour.TourService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/tours")
public class TourController {
    private final TourService tourService;
    private final ClientRequestService requestService;

    public TourController(TourService tourService, ClientRequestService requestService) {
        this.tourService = tourService;
        this.requestService = requestService;
    }

    @GetMapping
    public ResponseEntity<Page<TourDto>> getAllTours(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        // Если есть фильтры, используем findWithFilters, иначе только активные туры
        Page<TourDto> tours;
        if (destination != null || minPrice != null || maxPrice != null) {
            tours = tourService.findWithFilters(destination, minPrice, maxPrice, pageable);
        } else {
            tours = tourService.findActiveTours(pageable);
        }
        
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TourDto>> searchTours(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        Page<TourDto> tours = tourService.findWithFilters(destination, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(tours);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourDto> getTourById(@PathVariable Long id) {
        return tourService.findById(id)
                .map(TourMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/flights")
    public ResponseEntity<List<FlightDto>> getTourFlights(@PathVariable Long id) {
        try {
            List<Flight> flights = tourService.getTourFlights(id);
            List<FlightDto> flightDtos = flights.stream()
                    .map(com.example.airline.mapper.flight.FlightMapper::toDTO)
                    .toList();
            return ResponseEntity.ok(flightDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Создание заявки на тур (только для авторизованных клиентов).
     */
    @PostMapping("/{id}/request")
    public ResponseEntity<ClientRequestDto> submitRequest(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequestDto requestDto,
            Authentication authentication) {
        try {
            requestDto.setTourId(id);
            
            // Получаем username текущего пользователя для связи с клиентом
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            
            ClientRequestDto created = requestService.createRequestForUser(requestDto, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

