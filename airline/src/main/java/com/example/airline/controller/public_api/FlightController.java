package com.example.airline.controller.public_api;

import com.example.airline.dto.flight.FlightDto;
import com.example.airline.entity.flight.Airport;
import com.example.airline.entity.flight.Flight;
import com.example.airline.service.flight.AirportService;
import com.example.airline.service.flight.FlightService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private final FlightService flightService;
    private final AirportService airportService;

    public FlightController(FlightService flightService, AirportService airportService) {
        this.flightService = flightService;
        this.airportService = airportService;
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addFlight(@Valid @RequestBody FlightDto flightDto) {
        Flight flight = flightService.createFlightFromDto(flightDto);
        flightService.save(flight);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/search/by-airports")
    public ResponseEntity<Page<FlightDto>> searchFlightsByAirports(
            @RequestParam("departure") String departureAirportCode,
            @RequestParam("arrival") String arrivalAirportCode,
            @RequestParam(defaultValue = "1") int page) {

        Airport departureAirport = airportService.findById(departureAirportCode)
                .orElseThrow(() -> new RuntimeException("Departure airport not found"));

        Airport arrivalAirport = airportService.findById(arrivalAirportCode)
                .orElseThrow(() -> new RuntimeException("Arrival airport not found"));

        Page<FlightDto> flightDtoPage = flightService.getFlightDTOsByAirports(departureAirport, arrivalAirport, page);

        return ResponseEntity.ok(flightDtoPage);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Flight>> searchFlightsByDateRange(
            @RequestParam("from") @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate fromDate,
            @RequestParam("to") @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate toDate) {

        List<Flight> flights = flightService.findByDateRange(fromDate, toDate);

        if (flights.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(flights);
    }

    @PutMapping("/update/{flight_id}")
    public ResponseEntity<Void> updateFlight(
            @PathVariable("flight_id") int flightId,
            @Valid @RequestBody FlightDto flightDto) {

        Optional<Flight> flightOptional = flightService.findById(flightId);

        if (flightOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Flight flight = flightOptional.get();
        flightService.update(flightDto, flightId);
        flightService.updateFlightFromDto(flightDto, flight);

        return ResponseEntity.ok().build();
    }
}

