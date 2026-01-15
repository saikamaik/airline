package com.example.airline.service.flight;

import com.example.airline.entity.flight.Airport;
import com.example.airline.repository.flight.AirportRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AirportService {
    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public Optional<Airport> findById(String airportCode) {
        return airportRepository.findById(airportCode);
    }
}

