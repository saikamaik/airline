package com.example.airline.entity.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZoneId;

@Entity
@Table(name = "airports_data")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Airport {
    @Id
    @Column(name = "airport_code")
    private String airportCode;
    @Column(name = "airport_name", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private LocalizedAirportName airportName;
    @Column(name = "city", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private LocalizedCityName city;
    
    @Column(name = "timezone")
    private ZoneId timezone;

    public Airport(
            String airportCode,
            LocalizedAirportName airportName,
            LocalizedCityName city,
            ZoneId timezone) {
        this.airportCode = airportCode;
        this.airportName = airportName;
        this.city = city;
        this.timezone = timezone;
    }

    public Airport() {
    }

    public String getAirportCode() {
        return airportCode;
    }

    public void setAirportCode(String airportCode) {
        this.airportCode = airportCode;
    }

    public LocalizedAirportName getAirportName() {
        return airportName;
    }

    public void setAirportName(LocalizedAirportName airportName) {
        this.airportName = airportName;
    }

    public LocalizedCityName getCity() {
        return city;
    }

    public void setCity(LocalizedCityName city) {
        this.city = city;
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }
}

