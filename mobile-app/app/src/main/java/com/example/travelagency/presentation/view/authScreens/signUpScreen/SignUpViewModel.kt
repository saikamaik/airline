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
        val error = when {
            username.isBlank() -> null
            username.length < 3 -> "Логин должен быть не менее 3 символов"
            username.length > 50 -> "Логин должен быть не более 50 символов"
            else -> null
        }
        _uiState.value = _uiState.value.copy(username = username, usernameError = error)
    }

    private fun onPasswordChange(password: String) {
        val error = when {
            password.isBlank() -> null
            password.length < 6 -> "Пароль должен быть не менее 6 символов"
            password.length > 100 -> "Пароль должен быть не более 100 символов"
            else -> null
        }
        _uiState.value = _uiState.value.copy(password = password, passwordError = error)
    }

    private fun onEmailChange(email: String) {
        val error = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Некорректный email"
            else -> null
        }
        _uiState.value = _uiState.value.copy(email = email, emailError = error)
    }

    private fun onFirstNameChange(firstName: String) {
        val error = when {
            firstName.isBlank() -> null
            firstName.length < 2 -> "Имя должно быть не менее 2 символов"
            else -> null
        }
        _uiState.value = _uiState.value.copy(firstName = firstName, firstNameError = error)
    }

    private fun onLastNameChange(lastName: String) {
        val error = when {
            lastName.isBlank() -> null
            lastName.length < 2 -> "Фамилия должна быть не менее 2 символов"
            else -> null
        }
        _uiState.value = _uiState.value.copy(lastName = lastName, lastNameError = error)
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

        // Проверяем ошибки валидации полей
        val hasFieldErrors = state.usernameError != null || state.passwordError != null ||
            state.emailError != null || state.firstNameError != null || state.lastNameError != null

        // Валидация обязательных полей
        if (state.username.isBlank() || state.password.isBlank() ||
            state.email.isBlank() || state.firstName.isBlank() || state.lastName.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Заполните все обязательные поля")
            return@launch
        }

        // Если есть ошибки валидации полей, не отправляем запрос
        if (hasFieldErrors) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Исправьте ошибки в заполненных полях"
            )
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
