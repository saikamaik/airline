package com.example.airline.service.tour;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.entity.flight.Flight;
import com.example.airline.entity.tour.Tour;
import com.example.airline.mapper.tour.TourMapper;
import com.example.airline.repository.flight.FlightRepository;
import com.example.airline.repository.tour.TourRepository;
import com.example.airline.util.CustomValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class TourService {
    private final TourRepository tourRepository;
    private final FlightRepository flightRepository;
    private final CustomValidator customValidator;

    public TourService(TourRepository tourRepository, 
                      FlightRepository flightRepository,
                      CustomValidator customValidator) {
        this.tourRepository = tourRepository;
        this.flightRepository = flightRepository;
        this.customValidator = customValidator;
    }

    @Transactional(readOnly = true)
    public Optional<Tour> findById(Long id) {
        return tourRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<TourDto> findActiveTours(Pageable pageable) {
        return tourRepository.findByActiveTrue(pageable)
                .map(TourMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TourDto> findWithFilters(String destination, BigDecimal minPrice, 
                                         BigDecimal maxPrice, Pageable pageable) {
        // Преобразуем null в пустую строку, чтобы избежать проблем с типами в JPQL
        String destinationParam = (destination == null || destination.trim().isEmpty()) ? "" : destination;
        return tourRepository.findWithFilters(destinationParam, minPrice, maxPrice, pageable)
                .map(TourMapper::toDto);
    }

    public TourDto createTour(TourDto dto) {
        customValidator.validate(dto);

        Tour tour = TourMapper.toEntity(dto);
        
        // Attach flights if provided
        if (dto.getFlightIds() != null && !dto.getFlightIds().isEmpty()) {
            Set<Flight> flights = new HashSet<>();
            for (Integer flightId : dto.getFlightIds()) {
                Flight flight = flightRepository.findById(flightId)
                        .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightId));
                flights.add(flight);
            }
            tour.setFlights(flights);
        }

        tour = tourRepository.save(tour);
        return TourMapper.toDto(tour);
    }

    public TourDto updateTour(Long id, TourDto dto) {
        customValidator.validate(dto);

        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + id));

        TourMapper.updateEntity(tour, dto);

        // Update flights
        if (dto.getFlightIds() != null) {
            Set<Flight> flights = new HashSet<>();
            for (Integer flightId : dto.getFlightIds()) {
                Flight flight = flightRepository.findById(flightId)
                        .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + flightId));
                flights.add(flight);
            }
            tour.setFlights(flights);
        }

        tour = tourRepository.save(tour);
        return TourMapper.toDto(tour);
    }

    public void deleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + id));
        tour.setActive(false);
        tourRepository.save(tour);
    }

    @Transactional(readOnly = true)
    public List<Flight> getTourFlights(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new IllegalArgumentException("Tour not found: " + tourId));
        return List.copyOf(tour.getFlights());
    }
}

