package com.example.travelagency.presentation.view.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.presentation.view.homeScreen.uiEvent.HomeUiEvent
import com.example.travelagency.presentation.view.homeScreen.uiState.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tourRepository: TourRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState

    fun postUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnSearchQueryChange -> onSearchQueryChange(event.query)
            is HomeUiEvent.OnSearch -> searchTours()
            is HomeUiEvent.OnRefresh -> refreshTours()
            is HomeUiEvent.LoadMoreTours -> loadMoreTours()
        }
    }

    init {
        loadTours()
    }

    private fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun loadTours() = viewModelScope.launch {
        tourRepository.getAllTours(page = 0, size = 20).collect { response ->
            _uiState.value = _uiState.value.copy(toursResponse = response)
        }
    }

    private fun searchTours() = viewModelScope.launch {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadTours()
            return@launch
        }

        tourRepository.searchTours(
            destination = query,
            minPrice = null,
            maxPrice = null,
            page = 0,
            size = 20
        ).collect { response ->
            _uiState.value = _uiState.value.copy(toursResponse = response)
        }
    }

    private fun refreshTours() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        if (_uiState.value.searchQuery.isBlank()) {
            loadTours()
        } else {
            searchTours()
        }
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }

    private fun loadMoreTours() {
        // TODO: Implement pagination
    }

}
