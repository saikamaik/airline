package com.example.airline.mapper.flight;


import com.example.airline.dto.flight.FlightDto;
import com.example.airline.entity.flight.Flight;

public class FlightMapper {
    public static FlightDto toDTO(Flight flight) {
        FlightDto dto = new FlightDto();
        dto.setFlightNo(flight.getFlightNumber());
        dto.setScheduledDeparture(flight.getScheduledDeparture());
        dto.setScheduledArrival(flight.getScheduledArrival());
        dto.setDepartureAirportCode(flight.getDepartureAirport().getAirportCode());
        dto.setArrivalAirportCode(flight.getArrivalAirport().getAirportCode());
        dto.setStatus(flight.getStatus());
        dto.setAircraftCode(flight.getAircraft().getAircraftCode());
        return dto;
    }
}

