package com.example.travelagency.presentation.view.authScreens.signUpScreen.uiState

import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.SignUpResponse

data class SignUpUiState(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val isPasswordVisible: Boolean = false,
    val signUpResponse: SignUpResponse = Response.Loading,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Ошибки валидации для каждого поля
    val usernameError: String? = null,
    val passwordError: String? = null,
    val emailError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null
)
