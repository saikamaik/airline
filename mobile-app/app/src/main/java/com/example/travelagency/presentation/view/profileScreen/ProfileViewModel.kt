package com.example.travelagency.presentation.view.profileScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.presentation.view.profileScreen.uiState.ProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val user = authRepository.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            username = user?.username ?: ""
        )
    }

    fun logout() {
        authRepository.logout()
    }
}
