package com.example.airline.controller.public_api;

import com.example.airline.service.ml.MlServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Контроллер рекомендаций для мобильного приложения
 */
@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final MlServiceClient mlServiceClient;

    public RecommendationController(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    /**
     * Получить персонализированные рекомендации
     */
    @GetMapping
    public Mono<ResponseEntity<JsonNode>> getRecommendations(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) List<String> destinations,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer duration,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return mlServiceClient.getRecommendations(
                userId, destinations, minPrice, maxPrice, duration, limit
        )
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * Получить похожие туры
     */
    @GetMapping("/similar/{tourId}")
    public Mono<ResponseEntity<JsonNode>> getSimilarTours(
            @PathVariable int tourId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return mlServiceClient.getSimilarTours(tourId, limit)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }

    /**
     * Получить популярные туры
     */
    @GetMapping("/popular")
    public Mono<ResponseEntity<JsonNode>> getPopularTours(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return mlServiceClient.getPopularTours(limit)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }
}
