package com.example.airline.entity.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@JsonDeserialize(builder = Flight.Builder.class)
@Entity
@Table(name = "flights")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Flight {
    @Id
    @Column(name = "flight_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer flightId;
    @Column(name = "flight_no")
    private String flightNumber;
    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;
    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport")
    private Airport departureAirport;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_airport")
    private Airport arrivalAirport;
    @Convert(converter = StatusConverter.class)
    @Column(name = "status")
    private Status status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_code")
    private Aircraft aircraft;
    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;
    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    public Flight(Builder builder) {
        this.flightId = builder.flightId;
        this.flightNumber = builder.flightNumber;
        this.scheduledDeparture = builder.scheduledDeparture;
        this.scheduledArrival = builder.scheduledArrival;
        this.departureAirport = builder.departureAirport;
        this.arrivalAirport = builder.arrivalAirport;
        this.status = builder.status;
        this.aircraft = builder.aircraftCode;
        this.actualDeparture = builder.actualDeparture;
        this.actualArrival = builder.actualArrival;
    }

    public Flight() {

    }

    public static class Builder {
        private Integer flightId;
        private String flightNumber;
        private LocalDateTime scheduledDeparture;
        private LocalDateTime scheduledArrival;
        private Airport departureAirport;
        private Airport arrivalAirport;
        private Status status;
        private Aircraft aircraftCode;
        private LocalDateTime actualDeparture;
        private LocalDateTime actualArrival;

        public Builder flightId(Integer flightId) {
            this.flightId = flightId;
            return this;
        }

        @JsonProperty("flightNo")
        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        @JsonProperty("scheduledDeparture")
        public Builder scheduledDeparture(LocalDateTime scheduledDeparture) {
            this.scheduledDeparture = scheduledDeparture;
            return this;
        }

        @JsonProperty("scheduledArrival")
        public Builder scheduledArrival(LocalDateTime scheduledArrival) {
            this.scheduledArrival = scheduledArrival;
            return this;
        }

        @JsonProperty("departureAirport")
        public Builder departureAirport(Airport departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        @JsonProperty("arrivalAirport")
        public Builder arrivalAirport(Airport arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
            return this;
        }

        @JsonProperty("status")
        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        @JsonProperty("aircraft")
        public Builder aircraftCode(Aircraft aircraftCode) {
            this.aircraftCode = aircraftCode;
            return this;
        }

        @JsonProperty("actualDeparture")
        public Builder actualDeparture(LocalDateTime actualDeparture) {
            this.actualDeparture = actualDeparture;
            return this;
        }

        @JsonProperty("actualArrival")
        public Builder actualArrival(LocalDateTime actualArrival) {
            this.actualArrival = actualArrival;
            return this;
        }

        public Flight build() {
            return new Flight(this);
        }
    }

    public int getFlightId() {
        return flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public LocalDateTime getScheduledDeparture() {
        return scheduledDeparture;
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public Status getStatus() {
        return status;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public LocalDateTime getActualDeparture() {
        return actualDeparture;
    }

    public LocalDateTime getActualArrival() {
        return actualArrival;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setScheduledDeparture(LocalDateTime scheduledDeparture) {
        this.scheduledDeparture = scheduledDeparture;
    }

    public void setScheduledArrival(LocalDateTime scheduledArrival) {
        this.scheduledArrival = scheduledArrival;
    }

    public void setDepartureAirport(Airport departureAirport) {
        this.departureAirport = departureAirport;
    }

    public void setArrivalAirport(Airport arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setAircraft(Aircraft aircraftCode) {
        this.aircraft = aircraftCode;
    }

    public void setActualDeparture(LocalDateTime actualDeparture) {
        this.actualDeparture = actualDeparture;
    }

    public void setActualArrival(LocalDateTime actualArrival) {
        this.actualArrival = actualArrival;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }
}
