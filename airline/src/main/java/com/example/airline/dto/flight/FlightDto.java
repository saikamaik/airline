package com.example.airline.dto.flight;

import com.example.airline.entity.flight.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class FlightDto {
    @NotNull
    @Size(message = "flightNo need have max 6 characters", max = 6)
    private String flightNo;
    @NotNull
    private LocalDateTime scheduledDeparture;
    @NotNull
    private LocalDateTime scheduledArrival;
    @NotNull
    @Size(message = "departureAirport_code need have max 3 characters", max = 3)
    private String departureAirportCode;
    @NotNull
    @Size(message = "arrivalAirport_code need have max 3 characters", max = 3)
    private String arrivalAirportCode;
    @NotNull
    private Status status;
    @NotNull
    @Size(message = "aircraftCode need have max 3 characters", max = 3)
    private String aircraftCode;
    private LocalDateTime actualDeparture;
    private LocalDateTime actualArrival;

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public void setScheduledArrival(LocalDateTime scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public String getDepartureAirportCode() {
        return departureAirportCode;
    }

    public void setDepartureAirportCode(String departureAirportCode) {
        this.departureAirportCode = departureAirportCode;
    }

    public String getArrivalAirportCode() {
        return arrivalAirportCode;
    }

    public void setArrivalAirportCode(String arrivalAirportCode) {
        this.arrivalAirportCode = arrivalAirportCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public LocalDateTime getActualDeparture() {
        return actualDeparture;
    }

    public void setActualDeparture(LocalDateTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public LocalDateTime getActualArrival() {
        return actualArrival;
    }

    public void setActualArrival(LocalDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }
}

