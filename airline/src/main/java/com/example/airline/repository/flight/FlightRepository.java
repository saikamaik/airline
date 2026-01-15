package com.example.airline.repository.flight;

import com.example.airline.entity.flight.Airport;
import com.example.airline.entity.flight.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Integer> {

    // Поиск рейсов по диапазону дат с JOIN FETCH
    @Query("SELECT DISTINCT f FROM Flight f " +
            "JOIN FETCH f.aircraft " +
            "JOIN FETCH f.departureAirport " +
            "JOIN FETCH f.arrivalAirport " +
            "WHERE FUNCTION('DATE', f.scheduledDeparture) BETWEEN :from AND :to")
    List<Flight> findByDateRange(@Param("from") LocalDate from,
                                 @Param("to") LocalDate to);

    // Поиск рейсов по аэропортам с пагинацией
    @EntityGraph(attributePaths = {"aircraft", "departureAirport", "arrivalAirport"})
    Page<Flight> findByDepartureAirportAndArrivalAirport(
            Airport departure,
            Airport arrival,
            Pageable pageable);

    long countByDepartureAirportAndArrivalAirport(
            Airport departure,
            Airport arrival);
}

