package com.example.travelagency.presentation.view.authScreens.signUpScreen.uiEvent

sealed class SignUpUiEvent {
    data class OnUsernameChange(val username: String) : SignUpUiEvent()
    data class OnPasswordChange(val password: String) : SignUpUiEvent()
    data class OnEmailChange(val email: String) : SignUpUiEvent()
    data class OnFirstNameChange(val firstName: String) : SignUpUiEvent()
    data class OnLastNameChange(val lastName: String) : SignUpUiEvent()
    data class OnPhoneChange(val phone: String) : SignUpUiEvent()
    data object TogglePasswordVisibility : SignUpUiEvent()
    data object OnSignUpClick : SignUpUiEvent()
    data object ClearError : SignUpUiEvent()
}
