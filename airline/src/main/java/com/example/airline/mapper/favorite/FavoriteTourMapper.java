package com.example.airline.mapper.favorite;

import com.example.airline.dto.favorite.FavoriteTourDto;
import com.example.airline.entity.client.FavoriteTour;
import com.example.airline.mapper.tour.TourMapper;

public class FavoriteTourMapper {
    
    public static FavoriteTourDto toDto(FavoriteTour entity) {
        if (entity == null) {
            return null;
        }
        
        return FavoriteTourDto.builder()
                .id(entity.getId())
                .clientId(entity.getClient().getId())
                .tour(TourMapper.toDto(entity.getTour()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
