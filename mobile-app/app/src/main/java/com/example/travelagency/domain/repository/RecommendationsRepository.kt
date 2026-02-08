package com.example.travelagency.domain.repository

import com.example.travelagency.data.model.TourRecommendation

/**
 * Repository для работы с рекомендациями туров
 */
interface RecommendationsRepository {
    /**
     * Получить персонализированные рекомендации туров
     * @param userId ID пользователя для персонализации (null для неавторизованных)
     * @param limit Количество рекомендаций
     */
    suspend fun getRecommendations(
        userId: Long? = null,
        limit: Int = 5
    ): Result<List<TourRecommendation>>
}
