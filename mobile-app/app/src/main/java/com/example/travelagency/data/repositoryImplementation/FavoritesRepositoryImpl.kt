package com.example.travelagency.data.repositoryImplementation

import android.util.Log
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.model.AddToFavoritesRequest
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val TAG = "FavoritesRepository"

class FavoritesRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FavoritesRepository {

    override fun getFavorites(page: Int, size: Int): Flow<FavoritesResponse> = flow {
        Log.d(TAG, "getFavorites() called: page=$page, size=$size")
        emit(Response.Loading)

        try {
            val response = apiService.getFavorites(page = page, size = size)
            Log.d(TAG, "API response received: code=${response.code()}, isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "Favorites loaded successfully: ${body.content.size} items")
                emit(Response.Success(body.content))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Failed to load favorites: code=${response.code()}, errorBody=$errorBody")
                emit(Response.Failure(e = "Ошибка загрузки избранного: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getFavorites()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun addToFavorites(tourId: Long): Flow<FavoriteResponse> = flow {
        Log.d(TAG, "addToFavorites() called: tourId=$tourId")
        emit(Response.Loading)

        try {
            val request = AddToFavoritesRequest(tourId = tourId)
            val response = apiService.addToFavorites(request)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Tour added to favorites successfully")
                emit(Response.Success(response.body()!!))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Failed to add to favorites: code=${response.code()}, errorBody=$errorBody")
                emit(Response.Failure(e = "Ошибка добавления в избранное: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in addToFavorites()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun removeFromFavorites(tourId: Long): Flow<Response<Unit>> = flow {
        Log.d(TAG, "removeFromFavorites() called: tourId=$tourId")
        emit(Response.Loading)

        try {
            val response = apiService.removeFromFavorites(tourId)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Tour removed from favorites successfully")
                emit(Response.Success(Unit))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Failed to remove from favorites: code=${response.code()}, errorBody=$errorBody")
                emit(Response.Failure(e = "Ошибка удаления из избранного: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in removeFromFavorites()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun checkIsFavorite(tourId: Long): Flow<IsFavoriteResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.checkIsFavorite(tourId)
            
            if (response.isSuccessful && response.body() != null) {
                val isFavorite = response.body()!!.isFavorite
                emit(Response.Success(isFavorite))
            } else {
                emit(Response.Failure(e = "Ошибка проверки избранного"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in checkIsFavorite()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getFavoritesCount(): Flow<FavoritesCountResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getFavoritesCount()
            
            if (response.isSuccessful && response.body() != null) {
                val count = response.body()!!.count
                emit(Response.Success(count))
            } else {
                emit(Response.Failure(e = "Ошибка получения количества"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getFavoritesCount()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }
}
