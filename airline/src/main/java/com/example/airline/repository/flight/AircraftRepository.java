package com.example.airline.repository.flight;

import com.example.airline.entity.flight.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftRepository extends JpaRepository<Aircraft, String> {
}

