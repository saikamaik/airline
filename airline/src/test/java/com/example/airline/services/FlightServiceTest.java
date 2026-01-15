package com.example.airline.services;

import com.example.airline.dto.FlightDto;
import com.example.airline.entity.aircraft.Aircraft;
import com.example.airline.entity.aircraft.Model;
import com.example.airline.entity.airoport.Airport;
import com.example.airline.entity.airoport.LocalizedAirportName;
import com.example.airline.entity.airoport.LocalizedCityName;
import com.example.airline.entity.flight.Flight;
import com.example.airline.entity.flight.Status;
import com.example.airline.repositories.FlightRepository;
import com.example.airline.util.CustomValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;
    @Mock
    private CustomValidator customValidator;
    @Mock
    private AircraftService aircraftService;
    @Mock
    private AirportService airportService;

    @InjectMocks
    private FlightService flightService;

    @Test
    void getFlightDTOsByAirports_returnsMappedDtos() {
        Airport departure = buildAirport("SVO", "Sheremetyevo", "Moscow");
        Airport arrival = buildAirport("HKT", "Phuket", "Phuket");
        Aircraft aircraft = new Aircraft("320", new Model("Airbus A320", "Airbus A320"), 6100);
        Flight flight = new Flight.Builder()
                .flightId(1)
                .flightNumber("SU100")
                .scheduledDeparture(LocalDateTime.now().plusDays(1))
                .scheduledArrival(LocalDateTime.now().plusDays(1).plusHours(2))
                .departureAirport(departure)
                .arrivalAirport(arrival)
                .status(Status.SCHEDULED)
                .aircraftCode(aircraft)
                .build();
        Page<Flight> page = new PageImpl<>(List.of(flight));
        when(flightRepository.findByDepartureAirportAndArrivalAirport(eq(departure), eq(arrival), any(Pageable.class)))
                .thenReturn(page);

        Page<FlightDto> dtosPage = flightService.getFlightDTOsByAirports(departure, arrival, 1);

        assertThat(dtosPage.getContent()).hasSize(1);
        assertThat(dtosPage.getContent().get(0).getFlightNo()).isEqualTo("SU100");
        verify(flightRepository).findByDepartureAirportAndArrivalAirport(eq(departure), eq(arrival), any(Pageable.class));
    }

    @Test
    void createFlightFromDto_persistsFlight() {
        FlightDto flightDto = new FlightDto();
        flightDto.setFlightNo("SU200");
        flightDto.setScheduledDeparture(LocalDateTime.now().plusDays(2));
        flightDto.setScheduledArrival(LocalDateTime.now().plusDays(2).plusHours(3));
        flightDto.setDepartureAirportCode("SVO");
        flightDto.setArrivalAirportCode("LED");
        flightDto.setStatus(Status.SCHEDULED);
        flightDto.setAircraftCode("320");

        Airport departure = buildAirport("SVO", "Sheremetyevo", "Moscow");
        Airport arrival = buildAirport("LED", "Pulkovo", "Saint Petersburg");
        Aircraft aircraft = new Aircraft("320", new Model("Airbus A320", "Airbus A320"), 6100);

        when(airportService.findById("SVO")).thenReturn(Optional.of(departure));
        when(airportService.findById("LED")).thenReturn(Optional.of(arrival));
        when(aircraftService.findById("320")).thenReturn(Optional.of(aircraft));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Flight savedFlight = flightService.createFlightFromDto(flightDto);

        assertThat(savedFlight.getFlightNumber()).isEqualTo("SU200");
        verify(customValidator).validate(flightDto);
        verify(flightRepository).save(any(Flight.class));
    }

    private Airport buildAirport(String code, String airportName, String cityName) {
        return new Airport(
                code,
                new LocalizedAirportName(airportName, airportName),
                new LocalizedCityName(cityName, cityName),
                ZoneId.of("Europe/Moscow")
        );
    }
}

