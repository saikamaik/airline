package com.example.airline.service.flight;

import com.example.airline.dto.flight.FlightDto;
import com.example.airline.entity.flight.Aircraft;
import com.example.airline.entity.flight.Airport;
import com.example.airline.entity.flight.Flight;
import com.example.airline.mapper.flight.FlightMapper;
import com.example.airline.repository.flight.FlightRepository;
import com.example.airline.util.CustomValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class FlightService {
    private final FlightRepository flightRepository;
    private final CustomValidator customValidator;
    private final AircraftService aircraftService;
    private final AirportService airportService;
    private static final int PAGE_SIZE = 5;

    public FlightService(
            FlightRepository flightRepository,
            AirportService airportService,
            AircraftService aircraftService,
            CustomValidator customValidator) {
        this.flightRepository = flightRepository;
        this.customValidator = customValidator;
        this.aircraftService = aircraftService;
        this.airportService = airportService;
    }

    private static FlightDto getFlightDTO(Flight flight) {
        return FlightMapper.toDTO(flight);
    }

    public Optional<Flight> findById(Integer id) {
        return flightRepository.findById(id);
    }

    public List<Flight> findByDateRange(LocalDate from, LocalDate to) {
        return flightRepository.findByDateRange(from, to);
    }

    public Page<FlightDto> getFlightDTOsByAirports(Airport departure, Airport arrival, int pageNumber) {
        int safePage = Math.max(pageNumber - 1, 0);
        Pageable pageable = PageRequest.of(safePage, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "scheduledDeparture"));
        Page<Flight> flightsPage = flightRepository.findByDepartureAirportAndArrivalAirport(departure, arrival, pageable);
        return flightsPage.map(FlightService::getFlightDTO);
    }

    @Transactional
    public void save(Flight entity) {
        flightRepository.save(entity);
    }

    @Transactional
    public void update(FlightDto entityDTO, int flightId) {
        customValidator.validate(entityDTO);
        Flight flight = flightRepository.findById(flightId).orElseThrow(() ->
                new IllegalArgumentException("Flight not found with id: " + flightId)
        );
        updateFlightFromDto(entityDTO, flight);
        flightRepository.save(flight);
    }

    public void updateFlightFromDto(FlightDto flightDTO, Flight flight) {
        Map<String, Object> entities = loadEntity(flightDTO);
        Airport departureAirport = (Airport) entities.get("departureAirport");
        Airport arrivalAirport = (Airport) entities.get("arrivalAirport");
        Aircraft aircraft = (Aircraft) entities.get("aircraft");

        flight.setFlightNumber(flightDTO.getFlightNo());
        flight.setScheduledDeparture(flightDTO.getScheduledDeparture());
        flight.setScheduledArrival(flightDTO.getScheduledArrival());
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setStatus(flightDTO.getStatus());
        flight.setAircraft(aircraft);
    }

    @Transactional
    public Flight createFlightFromDto(FlightDto flightDTO) {
        customValidator.validate(flightDTO);
        Map<String, Object> entities = loadEntity(flightDTO);
        Airport departureAirport = (Airport) entities.get("departureAirport");
        Airport arrivalAirport = (Airport) entities.get("arrivalAirport");
        Aircraft aircraft = (Aircraft) entities.get("aircraft");

        Flight flight = new Flight.Builder()
                .flightNumber(flightDTO.getFlightNo())
                .scheduledDeparture(flightDTO.getScheduledDeparture())
                .scheduledArrival(flightDTO.getScheduledArrival())
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .status(flightDTO.getStatus())
                .aircraftCode(aircraft)
                .build();

        return flightRepository.save(flight);
    }

    private Map<String, Object> loadEntity(FlightDto flightDTO) {
        Map<String, Object> entities = new HashMap<>();

        Airport departureAirport = airportService.findById(flightDTO.getDepartureAirportCode())
                .orElseThrow(() -> new IllegalArgumentException("Departure airport not found"));
        Airport arrivalAirport = airportService.findById(flightDTO.getArrivalAirportCode())
                .orElseThrow(() -> new IllegalArgumentException("Arrival airport not found"));
        Aircraft aircraft = aircraftService.findById(flightDTO.getAircraftCode())
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));

        entities.put("departureAirport", departureAirport);
        entities.put("arrivalAirport", arrivalAirport);
        entities.put("aircraft", aircraft);

        return entities;
    }


    // Дополнительный метод для получения общего количества страниц
    public long getTotalPagesByAirports(Airport departure, Airport arrival) {
        long totalCount = flightRepository.countByDepartureAirportAndArrivalAirport(departure, arrival);
        return (long) Math.ceil((double) totalCount / PAGE_SIZE);
    }
}

