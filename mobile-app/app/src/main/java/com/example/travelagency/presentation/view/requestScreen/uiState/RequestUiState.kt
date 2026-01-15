package com.example.travelagency.presentation.view.requestScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.CreateRequestResponse
import com.example.travelagency.domain.TourResponse

data class RequestUiState(
    val tourResponse: TourResponse = Response.Loading,
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val comment: String = "",
    val createResponse: CreateRequestResponse? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
