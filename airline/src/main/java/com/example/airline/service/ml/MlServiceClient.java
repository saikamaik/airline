package com.example.airline.service.ml;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Клиент для взаимодействия с ML-сервисом
 */
@Service
public class MlServiceClient {

    private final WebClient webClient;

    public MlServiceClient(
            @Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    /**
     * Получить рекомендации туров
     */
    public Mono<JsonNode> getRecommendations(
            Long userId,
            List<String> preferredDestinations,
            Double minPrice,
            Double maxPrice,
            Integer preferredDuration,
            int limit
    ) {
        Map<String, Object> request = Map.of(
                "user_id", userId != null ? userId : 0,
                "preferred_destinations", preferredDestinations != null ? preferredDestinations : List.of(),
                "min_price", minPrice != null ? minPrice : 0,
                "max_price", maxPrice != null ? maxPrice : 999999,
                "preferred_duration", preferredDuration != null ? preferredDuration : 7,
                "limit", limit
        );

        return webClient.post()
                .uri("/recommendations/")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить похожие туры
     */
    public Mono<JsonNode> getSimilarTours(int tourId, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/recommendations/similar/{tourId}")
                        .queryParam("limit", limit)
                        .build(tourId))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить популярные туры
     */
    public Mono<JsonNode> getPopularTours(int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/recommendations/popular")
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить полную аналитику
     */
    public Mono<JsonNode> getAnalytics(String period) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/analytics/")
                        .queryParam("period", period)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить статистику по заявкам
     */
    public Mono<JsonNode> getStatistics(int days) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/analytics/statistics")
                        .queryParam("days", days)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить прогноз спроса
     */
    public Mono<JsonNode> getDemandForecast(String destination) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/analytics/forecast")
                            .queryParam("horizon_months", 6);  // Прогноз на 6 месяцев
                    if (destination != null && !destination.isEmpty()) {
                        builder.queryParam("destination", destination);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить прогноз спроса в табличном формате
     */
    public Mono<JsonNode> getDemandForecastTable() {
        return webClient.get()
                .uri("/analytics/forecast/table")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> org.slf4j.LoggerFactory.getLogger(MlServiceClient.class)
                        .error("Error getting demand forecast table: {}", e.getMessage()))
                .onErrorResume(e -> {
                    org.slf4j.LoggerFactory.getLogger(MlServiceClient.class)
                            .warn("ML service returned error for forecast table, returning empty");
                    return Mono.empty();
                });
    }

    /**
     * Получить все направления из базы данных
     */
    public Mono<JsonNode> getAllDestinations() {
        return webClient.get()
                .uri("/analytics/all-destinations")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить данные для дашборда
     */
    public Mono<JsonNode> getDashboardData() {
        return webClient.get()
                .uri("/analytics/dashboard")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Проверка доступности ML-сервиса
     */
    public Mono<Boolean> healthCheck() {
        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.just(false));
    }

    /**
     * Сбросить кэш рекомендаций
     */
    public Mono<Void> invalidateCache() {
        return webClient.post()
                .uri("/recommendations/invalidate-cache")
                .retrieve()
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить кластеры туров
     */
    public Mono<JsonNode> getTourClusters(int nClusters) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/analytics/clusters")
                        .queryParam("n_clusters", nClusters)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить метрики моделей
     */
    public Mono<JsonNode> getModelMetrics() {
        return webClient.get()
                .uri("/analytics/model-metrics")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить аномальные туры
     */
    public Mono<JsonNode> getAnomalousTours() {
        return webClient.get()
                .uri("/analytics/anomalies")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Получить сезонные тренды
     */
    public Mono<JsonNode> getSeasonalTrends(int months) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/analytics/trends")
                        .queryParam("months", months)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(15))
                .onErrorResume(e -> Mono.empty());
    }
}
