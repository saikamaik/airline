package com.example.travelagency.data.model

import com.google.gson.annotations.SerializedName

data class FavoriteTourModel(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("clientId")
    val clientId: Long,
    
    @SerializedName("tour")
    val tour: TourModel,
    
    @SerializedName("createdAt")
    val createdAt: String
)

data class FavoriteTourListResponse(
    @SerializedName("content")
    val content: List<FavoriteTourModel>,
    
    @SerializedName("totalElements")
    val totalElements: Long,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("last")
    val last: Boolean,
    
    @SerializedName("number")
    val number: Int,
    
    @SerializedName("size")
    val size: Int
)

data class AddToFavoritesRequest(
    @SerializedName("tourId")
    val tourId: Long
)

data class IsFavoriteResponse(
    @SerializedName("isFavorite")
    val isFavorite: Boolean
)

data class FavoritesCountResponse(
    @SerializedName("count")
    val count: Long
)
