package com.example.airline.dto.tour;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Set;

public class TourCategoryDto {
    private Long id;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    private String description;
    private String icon;
    private Boolean active = true;
    private Set<Long> tourIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TourCategoryDto() {
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Long> getTourIds() {
        return tourIds;
    }

    public void setTourIds(Set<Long> tourIds) {
        this.tourIds = tourIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

