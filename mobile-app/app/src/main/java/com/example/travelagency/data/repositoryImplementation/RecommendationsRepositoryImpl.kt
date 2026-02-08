package com.example.travelagency.data.repositoryImplementation

import android.util.Log
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.model.RecommendationRequest
import com.example.travelagency.data.model.TourRecommendation
import com.example.travelagency.domain.repository.RecommendationsRepository
import javax.inject.Inject

class RecommendationsRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : RecommendationsRepository {

    companion object {
        private const val TAG = "RecommendationsRepo"
    }

    override suspend fun getRecommendations(
        userId: Long?,
        limit: Int
    ): Result<List<TourRecommendation>> {
        return try {
            Log.d(TAG, "Fetching recommendations for user: $userId, limit: $limit")
            
            val request = RecommendationRequest(
                userId = userId,
                limit = limit
            )
            
            val response = apiService.getRecommendations(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "Successfully loaded ${body.recommendations.size} recommendations")
                    Result.success(body.recommendations)
                } else {
                    Log.e(TAG, "Response body is null")
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                val errorMsg = "Failed to load recommendations: ${response.code()} ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading recommendations", e)
            Result.failure(e)
        }
    }
}
