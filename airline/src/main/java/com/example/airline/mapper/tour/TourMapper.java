package com.example.airline.mapper.tour;

import com.example.airline.dto.tour.TourDto;
import com.example.airline.entity.tour.Tour;

import java.util.stream.Collectors;

public class TourMapper {
    
    public static TourDto toDto(Tour tour) {
        if (tour == null) {
            return null;
        }

        TourDto dto = new TourDto();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setPrice(tour.getPrice());
        dto.setDurationDays(tour.getDurationDays());
        dto.setImageUrl(tour.getImageUrl());
        dto.setDestinationCity(tour.getDestinationCity());
        dto.setActive(tour.isActive());
        dto.setCreatedAt(tour.getCreatedAt());
        dto.setUpdatedAt(tour.getUpdatedAt());

        if (tour.getFlights() != null && !tour.getFlights().isEmpty()) {
            dto.setFlightIds(tour.getFlights().stream()
                    .map(flight -> flight.getFlightId())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public static Tour toEntity(TourDto dto) {
        if (dto == null) {
            return null;
        }

        Tour tour = new Tour();
        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setPrice(dto.getPrice());
        tour.setDurationDays(dto.getDurationDays());
        tour.setImageUrl(dto.getImageUrl());
        tour.setDestinationCity(dto.getDestinationCity());
        tour.setActive(dto.isActive());

        return tour;
    }

    public static void updateEntity(Tour tour, TourDto dto) {
        if (tour == null || dto == null) {
            return;
        }

        tour.setName(dto.getName());
        tour.setDescription(dto.getDescription());
        tour.setPrice(dto.getPrice());
        tour.setDurationDays(dto.getDurationDays());
        tour.setImageUrl(dto.getImageUrl());
        tour.setDestinationCity(dto.getDestinationCity());
        tour.setActive(dto.isActive());
    }
}

