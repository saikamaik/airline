package com.example.travelagency.domain

import com.example.travelagency.data.model.AddToFavoritesRequest
import com.example.travelagency.data.model.FavoriteTourModel
import com.example.travelagency.data.model.Response
import kotlinx.coroutines.flow.Flow

typealias FavoritesResponse = Response<List<FavoriteTourModel>>
typealias FavoriteResponse = Response<FavoriteTourModel>
typealias IsFavoriteResponse = Response<Boolean>
typealias FavoritesCountResponse = Response<Long>

interface FavoritesRepository {
    
    fun getFavorites(page: Int, size: Int): Flow<FavoritesResponse>
    
    fun addToFavorites(tourId: Long): Flow<FavoriteResponse>
    
    fun removeFromFavorites(tourId: Long): Flow<Response<Unit>>
    
    fun checkIsFavorite(tourId: Long): Flow<IsFavoriteResponse>
    
    fun getFavoritesCount(): Flow<FavoritesCountResponse>
}
