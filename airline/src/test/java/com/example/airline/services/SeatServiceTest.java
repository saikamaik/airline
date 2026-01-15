package com.example.airline.services;

import com.example.airline.dto.SeatDto;
import com.example.airline.entity.aircraft.Aircraft;
import com.example.airline.entity.aircraft.Model;
import com.example.airline.entity.flight.Flight;
import com.example.airline.entity.flight.Status;
import com.example.airline.entity.ticket.Condition;
import com.example.airline.repositories.FlightRepository;
import com.example.airline.repositories.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;
    @Mock
    private AircraftService aircraftService;
    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private SeatService seatService;

    @Test
    void findFreeSeatsByFlightId_returnsAvailableSeats() {
        Aircraft aircraft = new Aircraft("320", new Model("Airbus A320", "Airbus A320"), 6100);
        Flight flight = new Flight.Builder()
                .flightId(10)
                .flightNumber("SU210")
                .scheduledDeparture(LocalDateTime.now())
                .scheduledArrival(LocalDateTime.now().plusHours(2))
                .status(Status.SCHEDULED)
                .aircraftCode(aircraft)
                .build();

        when(flightRepository.findById(anyInt())).thenReturn(Optional.of(flight));
        when(aircraftService.findById(eq("320"))).thenReturn(Optional.of(aircraft));
        List<SeatDto> expectedSeats = List.of(new SeatDto("1A", Condition.BUSINESS));
        when(seatRepository.findFreeSeatsByFlightId(eq("320"), eq(10))).thenReturn(expectedSeats);

        List<SeatDto> result = seatService.findFreeSeatsByFlightId(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo("1A");
        verify(seatRepository).findFreeSeatsByFlightId("320", 10);
    }
}

