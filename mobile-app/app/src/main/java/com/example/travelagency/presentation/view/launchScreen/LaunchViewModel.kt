package com.example.travelagency.presentation.view.launchScreen

import androidx.lifecycle.ViewModel
import com.example.travelagency.domain.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class LaunchUiState(
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<LaunchUiState> = MutableStateFlow(LaunchUiState())
    var uiState: StateFlow<LaunchUiState> = _uiState

    init {
        checkAuth()
    }

    private fun checkAuth() {
        _uiState.value = _uiState.value.copy(isLoggedIn = authRepository.isLoggedIn())
    }

}
