package com.example.airline.repository.flight;

import com.example.airline.entity.flight.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirportRepository extends JpaRepository<Airport, String> {
}

