package com.example.travelagency.presentation.view.myRequestsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.myRequestsScreen.uiEvent.MyRequestsUiEvent
import com.example.travelagency.presentation.view.myRequestsScreen.uiState.MyRequestsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val tourRepository: TourRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyRequestsUiState> = MutableStateFlow(MyRequestsUiState())
    var uiState: StateFlow<MyRequestsUiState> = _uiState

    fun postUiEvent(event: MyRequestsUiEvent) {
        when (event) {
            is MyRequestsUiEvent.Refresh -> loadRequests()
        }
    }

    init {
        loadRequests()
    }

    private fun loadRequests() = viewModelScope.launch {
        tourRepository.getMyRequests(page = 0, size = 50).collect { response ->
            _uiState.value = _uiState.value.copy(requestsResponse = response)
        }
    }

}
