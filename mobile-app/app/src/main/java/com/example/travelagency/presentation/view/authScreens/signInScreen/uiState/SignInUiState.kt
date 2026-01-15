package com.example.travelagency.presentation.view.authScreens.signInScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.SignInResponse

data class SignInUiState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val signInResponse: SignInResponse = Response.Loading,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
