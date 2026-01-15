package com.example.travelagency.presentation.view.requestScreen.uiEvent

sealed class RequestUiEvent {
    data class OnNameChange(val name: String) : RequestUiEvent()
    data class OnEmailChange(val email: String) : RequestUiEvent()
    data class OnPhoneChange(val phone: String) : RequestUiEvent()
    data class OnCommentChange(val comment: String) : RequestUiEvent()
    data object OnSubmit : RequestUiEvent()
    data object ClearError : RequestUiEvent()
}
