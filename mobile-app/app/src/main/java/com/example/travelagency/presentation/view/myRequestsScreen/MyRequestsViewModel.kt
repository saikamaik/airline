package com.example.travelagency.presentation.view.myRequestsScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.myRequestsScreen.uiEvent.MyRequestsUiEvent
import com.example.travelagency.presentation.view.myRequestsScreen.uiState.MyRequestsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MyRequestsViewModel"

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val tourRepository: TourRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyRequestsUiState> = MutableStateFlow(MyRequestsUiState())
    var uiState: StateFlow<MyRequestsUiState> = _uiState

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    fun postUiEvent(event: MyRequestsUiEvent) {
        when (event) {
            is MyRequestsUiEvent.Refresh -> loadRequests()
        }
    }

    init {
        checkAuthorizationAndLoad()
    }

    fun checkAuthorizationAndLoad() {
        Log.d(TAG, "checkAuthorizationAndLoad() called")
        checkAuthorization()
        Log.d(TAG, "isAuthorized: ${_isAuthorized.value}")
        if (_isAuthorized.value) {
            loadRequests()
        } else {
            Log.w(TAG, "User not authorized, skipping requests load")
        }
    }

    private fun checkAuthorization() {
        val user = authRepository.getCurrentUser()
        _isAuthorized.value = user != null
        Log.d(TAG, "checkAuthorization: user = ${user?.username}, isAuthorized = ${_isAuthorized.value}")
    }

    private fun loadRequests() = viewModelScope.launch {
        Log.d(TAG, "loadRequests() called")
        try {
            tourRepository.getMyRequests(page = 0, size = 50).collect { response ->
                Log.d(TAG, "Received response: ${response::class.simpleName}")
                _uiState.value = _uiState.value.copy(requestsResponse = response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading requests", e)
        }
    }

}
