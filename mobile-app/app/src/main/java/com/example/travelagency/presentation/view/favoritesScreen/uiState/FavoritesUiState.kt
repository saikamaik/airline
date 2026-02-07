package com.example.travelagency.presentation.view.favoritesScreen.uiState

import com.example.travelagency.data.model.FavoriteTourModel
import com.example.travelagency.data.model.Response

data class FavoritesUiState(
    val favorites: List<FavoriteTourModel> = emptyList(),
    val favoritesResponse: Response<List<FavoriteTourModel>> = Response.Loading,
    val isRefreshing: Boolean = false
)
