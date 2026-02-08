package com.example.travelagency.data.model

import com.google.gson.annotations.SerializedName

/**
 * Рекомендация тура
 */
data class TourRecommendation(
    @SerializedName("tour_id")
    val tourId: Long,
    @SerializedName("tour_name")
    val tourName: String,
    @SerializedName("destination")
    val destination: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("score")
    val score: Double,
    @SerializedName("reason")
    val reason: String
)

/**
 * Запрос на получение рекомендаций
 */
data class RecommendationRequest(
    @SerializedName("user_id")
    val userId: Long? = null,
    @SerializedName("preferred_destinations")
    val preferredDestinations: List<String>? = null,
    @SerializedName("min_price")
    val minPrice: Double? = null,
    @SerializedName("max_price")
    val maxPrice: Double? = null,
    @SerializedName("preferred_duration")
    val preferredDuration: Int? = null,
    @SerializedName("limit")
    val limit: Int = 5
)

/**
 * Ответ с рекомендациями
 */
data class RecommendationResponse(
    @SerializedName("recommendations")
    val recommendations: List<TourRecommendation>,
    @SerializedName("total_tours_analyzed")
    val totalToursAnalyzed: Int,
    @SerializedName("model_version")
    val modelVersion: String? = null
)
