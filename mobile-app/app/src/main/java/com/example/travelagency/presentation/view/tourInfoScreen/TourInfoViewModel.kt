package com.example.travelagency.presentation.view.tourInfoScreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.FavoritesRepository
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.tourInfoScreen.uiEvent.TourInfoUiEvent
import com.example.travelagency.presentation.view.tourInfoScreen.uiState.TourInfoUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TourInfoViewModel"

@HiltViewModel
class TourInfoViewModel @Inject constructor(
    private val tourRepository: TourRepository,
    private val favoritesRepository: FavoritesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tourId: Long = savedStateHandle.get<Long>("tourId") ?: 0L

    private val _uiState: MutableStateFlow<TourInfoUiState> = MutableStateFlow(TourInfoUiState())
    var uiState: StateFlow<TourInfoUiState> = _uiState

    fun postUiEvent(event: TourInfoUiEvent) {
        when (event) {
            is TourInfoUiEvent.LoadFlights -> loadFlights()
            is TourInfoUiEvent.ToggleFavorite -> toggleFavorite()
        }
    }

    init {
        loadTour()
        loadFlights()
        checkIsFavorite()
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

    private fun checkIsFavorite() = viewModelScope.launch {
        try {
            val response = favoritesRepository.checkIsFavorite(tourId).first()
            when (response) {
                is Response.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isFavorite = response.data,
                        isCheckingFavorite = false
                    )
                }
                else -> {
                    _uiState.value = _uiState.value.copy(isCheckingFavorite = false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking favorite status", e)
            _uiState.value = _uiState.value.copy(isCheckingFavorite = false)
        }
    }

    private fun toggleFavorite() = viewModelScope.launch {
        val currentState = _uiState.value.isFavorite
        
        try {
            if (currentState) {
                // Удаляем из избранного
                val response = favoritesRepository.removeFromFavorites(tourId).first()
                if (response is Response.Success) {
                    _uiState.value = _uiState.value.copy(isFavorite = false)
                    Log.d(TAG, "Removed from favorites")
                }
            } else {
                // Добавляем в избранное
                val response = favoritesRepository.addToFavorites(tourId).first()
                if (response is Response.Success) {
                    _uiState.value = _uiState.value.copy(isFavorite = true)
                    Log.d(TAG, "Added to favorites")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling favorite", e)
        }
    }

}
