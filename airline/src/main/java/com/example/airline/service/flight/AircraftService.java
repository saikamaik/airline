package com.example.airline.service.flight;

import com.example.airline.entity.flight.Aircraft;
import com.example.airline.repository.flight.AircraftRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AircraftService {
    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public Optional<Aircraft> findById(String aircraftCode) {
        return aircraftRepository.findById(aircraftCode);
    }
}

