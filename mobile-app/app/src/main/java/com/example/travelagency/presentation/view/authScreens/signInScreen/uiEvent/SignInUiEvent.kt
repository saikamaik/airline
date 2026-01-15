package com.example.travelagency.presentation.view.authScreens.signInScreen.uiEvent

sealed class SignInUiEvent {
    data class OnUsernameChange(val username: String) : SignInUiEvent()
    data class OnPasswordChange(val password: String) : SignInUiEvent()
    data object TogglePasswordVisibility : SignInUiEvent()
    data object OnSignInClick : SignInUiEvent()
    data object ClearError : SignInUiEvent()
}
