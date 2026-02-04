package com.example.airline.controller.admin;

import com.example.airline.service.ml.MlServiceClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * Контроллер аналитики для администраторов
 * Интеграция с ML-сервисом
 */
@RestController
@RequestMapping("/admin/analytics")
public class AdminAnalyticsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAnalyticsController.class);
    
    private final MlServiceClient mlServiceClient;

    public AdminAnalyticsController(MlServiceClient mlServiceClient) {
        this.mlServiceClient = mlServiceClient;
    }

    /**
     * Получить полную аналитику
     */
    @GetMapping
    public ResponseEntity<JsonNode> getFullAnalytics(
            @RequestParam(defaultValue = "month") String period
    ) {
        try {
            JsonNode result = mlServiceClient.getAnalytics(period)
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting full analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить данные для дашборда
     */
    @GetMapping("/dashboard")
    public ResponseEntity<JsonNode> getDashboard() {
        try {
            JsonNode result = mlServiceClient.getDashboardData()
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить статистику по заявкам
     */
    @GetMapping("/statistics")
    public ResponseEntity<JsonNode> getStatistics(
            @RequestParam(defaultValue = "30") int days
    ) {
        try {
            JsonNode result = mlServiceClient.getStatistics(days)
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить прогноз спроса
     */
    @GetMapping("/forecast")
    public ResponseEntity<JsonNode> getDemandForecast(
            @RequestParam(required = false) String destination
    ) {
        try {
            JsonNode result = mlServiceClient.getDemandForecast(destination)
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting demand forecast", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить прогноз спроса в табличном формате
     */
    @GetMapping("/forecast/table")
    public ResponseEntity<JsonNode> getDemandForecastTable() {
        try {
            JsonNode result = mlServiceClient.getDemandForecastTable()
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting demand forecast table", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Проверить доступность ML-сервиса
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkMlServiceHealth() {
        try {
            Boolean isHealthy = mlServiceClient.healthCheck()
                    .block(Duration.ofSeconds(5));
            return ResponseEntity.ok(Map.of(
                    "ml_service", isHealthy != null && isHealthy ? "available" : "unavailable",
                    "status", isHealthy != null && isHealthy ? "ok" : "degraded"
            ));
        } catch (Exception e) {
            logger.error("Error checking ML service health", e);
            return ResponseEntity.ok(Map.of(
                    "ml_service", "unavailable",
                    "status", "error"
            ));
        }
    }

    /**
     * Получить кластеры туров
     */
    @GetMapping("/clusters")
    public ResponseEntity<JsonNode> getTourClusters(
            @RequestParam(defaultValue = "3") int n_clusters
    ) {
        try {
            JsonNode result = mlServiceClient.getTourClusters(n_clusters)
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting tour clusters", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить метрики моделей
     */
    @GetMapping("/model-metrics")
    public ResponseEntity<JsonNode> getModelMetrics() {
        try {
            JsonNode result = mlServiceClient.getModelMetrics()
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting model metrics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить аномальные туры
     */
    @GetMapping("/anomalies")
    public ResponseEntity<JsonNode> getAnomalousTours() {
        try {
            JsonNode result = mlServiceClient.getAnomalousTours()
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting anomalous tours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить сезонные тренды
     */
    @GetMapping("/trends")
    public ResponseEntity<JsonNode> getSeasonalTrends(
            @RequestParam(defaultValue = "12") int months
    ) {
        try {
            JsonNode result = mlServiceClient.getSeasonalTrends(months)
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting seasonal trends", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получить все направления из базы данных
     */
    @GetMapping("/all-destinations")
    public ResponseEntity<JsonNode> getAllDestinations() {
        try {
            JsonNode result = mlServiceClient.getAllDestinations()
                    .block(Duration.ofSeconds(30));
            if (result != null) {
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            logger.error("Error getting all destinations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
