package com.example.airline.controller.admin;

import com.example.airline.service.ml.MlServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Контроллер аналитики для администраторов
 * Интеграция с ML-сервисом
 */
@RestController
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

    private final MlServiceClient mlServiceClient;

    public AdminAnalyticsController(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    /**
     * Получить полную аналитику
     */
    @GetMapping
    public Mono<ResponseEntity<JsonNode>> getFullAnalytics(
            @RequestParam(defaultValue = "month") String period
    ) {
        return mlServiceClient.getAnalytics(period)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.serviceUnavailable().build());
    }

    /**
     * Получить данные для дашборда
     */
    @GetMapping("/dashboard")
    public Mono<ResponseEntity<JsonNode>> getDashboard() {
        return mlServiceClient.getDashboardData()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.serviceUnavailable().build());
    }

    /**
     * Получить статистику по заявкам
     */
    @GetMapping("/statistics")
    public Mono<ResponseEntity<JsonNode>> getStatistics(
            @RequestParam(defaultValue = "30") int days
    ) {
        return mlServiceClient.getStatistics(days)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.serviceUnavailable().build());
    }

    /**
     * Получить прогноз спроса
     */
    @GetMapping("/forecast")
    public Mono<ResponseEntity<JsonNode>> getDemandForecast(
            @RequestParam(required = false) String destination
    ) {
        return mlServiceClient.getDemandForecast(destination)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.serviceUnavailable().build());
    }

    /**
     * Проверить доступность ML-сервиса
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkMlServiceHealth() {
        return mlServiceClient.healthCheck()
                .map(isHealthy -> ResponseEntity.ok(Map.of(
                        "ml_service", isHealthy ? "available" : "unavailable",
                        "status", isHealthy ? "ok" : "degraded"
                )));
    }
}
