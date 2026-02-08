package com.example.airline.controller.public_api;

import com.example.airline.service.ml.MlServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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
     * Получить персонализированные рекомендации (GET)
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
     * Получить персонализированные рекомендации (POST - для мобильного приложения)
     */
    @PostMapping
    public Mono<ResponseEntity<JsonNode>> getRecommendationsPost(@RequestBody Map<String, Object> request) {
        Long userId = request.get("user_id") != null ? 
            ((Number) request.get("user_id")).longValue() : null;
        
        @SuppressWarnings("unchecked")
        List<String> destinations = request.get("preferred_destinations") != null ?
            (List<String>) request.get("preferred_destinations") : null;
        
        Double minPrice = request.get("min_price") != null ?
            ((Number) request.get("min_price")).doubleValue() : null;
        
        Double maxPrice = request.get("max_price") != null ?
            ((Number) request.get("max_price")).doubleValue() : null;
        
        Integer duration = request.get("preferred_duration") != null ?
            ((Number) request.get("preferred_duration")).intValue() : null;
        
        int limit = request.get("limit") != null ?
            ((Number) request.get("limit")).intValue() : 5;
        
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
