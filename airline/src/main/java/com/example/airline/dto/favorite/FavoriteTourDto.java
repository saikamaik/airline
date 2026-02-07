package com.example.airline.dto.favorite;

import com.example.airline.dto.tour.TourDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteTourDto {
    private Long id;
    private Long clientId;
    private TourDto tour;
    private LocalDateTime createdAt;
}
