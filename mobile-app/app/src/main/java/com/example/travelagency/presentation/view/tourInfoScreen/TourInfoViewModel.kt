package com.example.travelagency.presentation.view.tourInfoScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.tourInfoScreen.uiEvent.TourInfoUiEvent
import com.example.travelagency.presentation.view.tourInfoScreen.uiState.TourInfoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TourInfoViewModel @Inject constructor(
    private val tourRepository: TourRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tourId: Long = savedStateHandle.get<Long>("tourId") ?: 0L

    private val _uiState: MutableStateFlow<TourInfoUiState> = MutableStateFlow(TourInfoUiState())
    var uiState: StateFlow<TourInfoUiState> = _uiState

    fun postUiEvent(event: TourInfoUiEvent) {
        when (event) {
            is TourInfoUiEvent.LoadFlights -> loadFlights()
        }
    }

    init {
        loadTour()
        loadFlights()
    }

    private fun loadTour() = viewModelScope.launch {
        tourRepository.getTourById(tourId).collect { response ->
            _uiState.value = _uiState.value.copy(tourResponse = response)
        }
    }

    private fun loadFlights() = viewModelScope.launch {
        tourRepository.getTourFlights(tourId).collect { response ->
            _uiState.value = _uiState.value.copy(flightsResponse = response)
        }
    }

}
