package com.example.airline.mapper.tour;

import com.example.airline.dto.tour.TourCategoryDto;
import com.example.airline.entity.tour.TourCategory;

import java.util.stream.Collectors;

public class TourCategoryMapper {
    
    public static TourCategoryDto toDto(TourCategory category) {
        if (category == null) {
            return null;
        }
        
        TourCategoryDto dto = new TourCategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setActive(category.getActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        
        if (category.getTours() != null) {
            dto.setTourIds(category.getTours().stream()
                    .map(tour -> tour.getId())
                    .collect(Collectors.toSet()));
        }
        
        return dto;
    }
    
    public static TourCategory toEntity(TourCategoryDto dto) {
        if (dto == null) {
            return null;
        }
        
        TourCategory category = new TourCategory();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setIcon(dto.getIcon());
        if (dto.getActive() != null) {
            category.setActive(dto.getActive());
        }
        
        return category;
    }
}

