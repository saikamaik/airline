package com.example.travelagency.presentation.view.favoritesScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.FavoritesRepository
import com.example.travelagency.presentation.view.favoritesScreen.uiEvent.FavoritesUiEvent
import com.example.travelagency.presentation.view.favoritesScreen.uiState.FavoritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FavoritesViewModel"

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<FavoritesUiState> = MutableStateFlow(FavoritesUiState())
    var uiState: StateFlow<FavoritesUiState> = _uiState

    init {
        Log.d(TAG, "FavoritesViewModel initialized, loading favorites...")
        loadFavorites()
    }

    fun postUiEvent(event: FavoritesUiEvent) {
        when (event) {
            is FavoritesUiEvent.OnRefresh -> refreshFavorites()
            is FavoritesUiEvent.OnRemoveFromFavorites -> removeFromFavorites(event.tourId)
        }
    }

    private fun loadFavorites() = viewModelScope.launch {
        Log.d(TAG, "loadFavorites() called")
        _uiState.value = _uiState.value.copy(
            favoritesResponse = Response.Loading
        )

        try {
            val response = favoritesRepository.getFavorites(page = 0, size = 100).first()
            Log.d(TAG, "Received response: ${response::class.simpleName}")
            
            when (response) {
                is Response.Loading -> {
                    _uiState.value = _uiState.value.copy(
                        favoritesResponse = Response.Loading
                    )
                }
                is Response.Success -> {
                    Log.d(TAG, "Response is Success: ${response.data.size} favorites")
                    _uiState.value = _uiState.value.copy(
                        favorites = response.data,
                        favoritesResponse = Response.Success(response.data)
                    )
                }
                is Response.Failure -> {
                    Log.e(TAG, "Response is Failure: ${response.e}")
                    _uiState.value = _uiState.value.copy(
                        favoritesResponse = Response.Failure(response.e)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in loadFavorites()", e)
            _uiState.value = _uiState.value.copy(
                favoritesResponse = Response.Failure(e.message ?: "Неизвестная ошибка")
            )
        }
    }

    private fun refreshFavorites() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadFavorites()
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }

    private fun removeFromFavorites(tourId: Long) = viewModelScope.launch {
        Log.d(TAG, "removeFromFavorites() called: tourId=$tourId")
        
        try {
            val response = favoritesRepository.removeFromFavorites(tourId).first()
            
            when (response) {
                is Response.Success -> {
                    Log.d(TAG, "Successfully removed from favorites")
                    // Обновляем список после удаления
                    loadFavorites()
                }
                is Response.Failure -> {
                    Log.e(TAG, "Failed to remove from favorites: ${response.e}")
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in removeFromFavorites()", e)
        }
    }
}
