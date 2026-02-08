package com.example.travelagency.presentation.view.homeScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.TourModel
import com.example.travelagency.domain.ToursResponse

data class HomeUiState(
    val toursResponse: ToursResponse = Response.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val tours: List<TourModel> = emptyList(),
    val currentPage: Int = 0,
    val hasMore: Boolean = true,
    val isLoadingMore: Boolean = false,
    val pageSize: Int = 20,
    // Фильтры
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val showFilters: Boolean = false
)
