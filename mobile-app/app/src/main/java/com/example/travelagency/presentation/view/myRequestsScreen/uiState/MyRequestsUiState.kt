package com.example.travelagency.presentation.view.myRequestsScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.RequestsResponse

data class MyRequestsUiState(
    val requestsResponse: RequestsResponse = Response.Loading
)
