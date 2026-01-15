package com.example.travelagency.presentation.view.requestScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.requestScreen.uiEvent.RequestUiEvent
import com.example.travelagency.presentation.view.requestScreen.uiState.RequestUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val tourRepository: TourRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tourId: Long = savedStateHandle.get<Long>("tourId") ?: 0L

    private val _uiState: MutableStateFlow<RequestUiState> = MutableStateFlow(RequestUiState())
    var uiState: StateFlow<RequestUiState> = _uiState

    fun postUiEvent(event: RequestUiEvent) {
        when (event) {
            is RequestUiEvent.OnNameChange -> onNameChange(event.name)
            is RequestUiEvent.OnEmailChange -> onEmailChange(event.email)
            is RequestUiEvent.OnPhoneChange -> onPhoneChange(event.phone)
            is RequestUiEvent.OnCommentChange -> onCommentChange(event.comment)
            is RequestUiEvent.OnSubmit -> submitRequest()
            is RequestUiEvent.ClearError -> clearError()
        }
    }

    init {
        loadTour()
        prefillUserData()
    }

    private fun loadTour() = viewModelScope.launch {
        tourRepository.getTourById(tourId).collect { response ->
            _uiState.value = _uiState.value.copy(tourResponse = response)
        }
    }

    private fun prefillUserData() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                userName = user.username
            )
        }
    }

    private fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    private fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(userEmail = email)
    }

    private fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(userPhone = phone)
    }

    private fun onCommentChange(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    private fun submitRequest() = viewModelScope.launch {
        val state = _uiState.value

        if (state.userName.isBlank() || state.userEmail.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Заполните обязательные поля (Имя и Email)"
            )
            return@launch
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.userEmail).matches()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Некорректный email")
            return@launch
        }

        tourRepository.createRequest(
            tourId = tourId,
            userName = state.userName,
            userEmail = state.userEmail,
            userPhone = state.userPhone.ifBlank { null },
            comment = state.comment.ifBlank { null }
        ).collect { response ->
            _uiState.value = _uiState.value.copy(
                createResponse = response,
                isLoading = response is Response.Loading,
                isSuccess = response is Response.Success,
                errorMessage = if (response is Response.Failure) response.e else null
            )
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}
