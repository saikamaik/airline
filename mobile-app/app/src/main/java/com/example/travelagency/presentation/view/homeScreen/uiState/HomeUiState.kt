package com.example.travelagency.presentation.view.homeScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.ToursResponse

data class HomeUiState(
    val toursResponse: ToursResponse = Response.Loading,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false
)
