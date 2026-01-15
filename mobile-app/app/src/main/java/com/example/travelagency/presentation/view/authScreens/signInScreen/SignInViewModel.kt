package com.example.travelagency.presentation.view.authScreens.signInScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.presentation.view.authScreens.signInScreen.uiEvent.SignInUiEvent
import com.example.travelagency.presentation.view.authScreens.signInScreen.uiState.SignInUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignInUiState> = MutableStateFlow(SignInUiState())
    var uiState: StateFlow<SignInUiState> = _uiState

    fun postUiEvent(event: SignInUiEvent) {
        when (event) {
            is SignInUiEvent.OnUsernameChange -> onUsernameChange(event.username)
            is SignInUiEvent.OnPasswordChange -> onPasswordChange(event.password)
            is SignInUiEvent.TogglePasswordVisibility -> togglePasswordVisibility()
            is SignInUiEvent.OnSignInClick -> signIn()
            is SignInUiEvent.ClearError -> clearError()
        }
    }

    private fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    private fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    private fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    private fun signIn() = viewModelScope.launch {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Заполните все поля")
            return@launch
        }

        authRepository.signIn(state.username, state.password).collect { response ->
            _uiState.value = _uiState.value.copy(
                signInResponse = response,
                isLoading = response is Response.Loading,
                errorMessage = if (response is Response.Failure) response.e else null
            )
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}
