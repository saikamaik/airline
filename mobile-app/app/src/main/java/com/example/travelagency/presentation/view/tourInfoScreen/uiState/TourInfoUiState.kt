package com.example.travelagency.presentation.view.tourInfoScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.FlightsResponse
import com.example.travelagency.domain.TourResponse

data class TourInfoUiState(
    val tourResponse: TourResponse = Response.Loading,
    val flightsResponse: FlightsResponse = Response.Loading
)
