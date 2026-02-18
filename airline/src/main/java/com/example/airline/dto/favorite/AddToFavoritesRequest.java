package com.example.airline.dto.favorite;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddToFavoritesRequest {
    private Long tourId;
}
