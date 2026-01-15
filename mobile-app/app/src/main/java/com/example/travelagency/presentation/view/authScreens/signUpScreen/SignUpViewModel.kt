package com.example.travelagency.presentation.view.authScreens.signUpScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.presentation.view.authScreens.signUpScreen.uiEvent.SignUpUiEvent
import com.example.travelagency.presentation.view.authScreens.signUpScreen.uiState.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignUpUiState> = MutableStateFlow(SignUpUiState())
    var uiState: StateFlow<SignUpUiState> = _uiState

    fun postUiEvent(event: SignUpUiEvent) {
        when (event) {
            is SignUpUiEvent.OnUsernameChange -> onUsernameChange(event.username)
            is SignUpUiEvent.OnPasswordChange -> onPasswordChange(event.password)
            is SignUpUiEvent.OnEmailChange -> onEmailChange(event.email)
            is SignUpUiEvent.OnFirstNameChange -> onFirstNameChange(event.firstName)
            is SignUpUiEvent.OnLastNameChange -> onLastNameChange(event.lastName)
            is SignUpUiEvent.OnPhoneChange -> onPhoneChange(event.phone)
            is SignUpUiEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            is SignUpUiEvent.OnSignUpClick -> signUp()
            is SignUpUiEvent.ClearError -> clearError()
        }
    }

    private fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    private fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    private fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    private fun onFirstNameChange(firstName: String) {
        _uiState.value = _uiState.value.copy(firstName = firstName)
    }

    private fun onLastNameChange(lastName: String) {
        _uiState.value = _uiState.value.copy(lastName = lastName)
    }

    private fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    private fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    private fun signUp() = viewModelScope.launch {
        val state = _uiState.value

        if (state.username.isBlank() || state.password.isBlank() ||
            state.email.isBlank() || state.firstName.isBlank() || state.lastName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Заполните все обязательные поля")
            return@launch
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Некорректный email")
            return@launch
        }

        if (state.password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Пароль должен быть не менее 6 символов")
            return@launch
        }

        authRepository.signUp(
            username = state.username,
            password = state.password,
            email = state.email,
            firstName = state.firstName,
            lastName = state.lastName,
            phone = state.phone.ifBlank { null }
        ).collect { response ->
            _uiState.value = _uiState.value.copy(
                signUpResponse = response,
                isLoading = response is Response.Loading,
                errorMessage = if (response is Response.Failure) response.e else null
            )
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}
