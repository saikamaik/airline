package com.example.travelagency.presentation.view.homeScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.domain.ToursResponse
import com.example.travelagency.presentation.view.homeScreen.uiEvent.HomeUiEvent
import com.example.travelagency.presentation.view.homeScreen.uiState.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

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
        Log.d(TAG, "HomeViewModel initialized, loading tours...")
        loadTours()
    }

    private fun onSearchQueryChange(query: String) {
        Log.d(TAG, "Search query changed: $query")
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun loadTours() = viewModelScope.launch {
        Log.d(TAG, "loadTours() called, page: 0, size: ${_uiState.value.pageSize}")
        _uiState.value = _uiState.value.copy(
            currentPage = 0,
            hasMore = true,
            tours = emptyList(),
            toursResponse = Response.Loading as ToursResponse
        )
        
        try {
            Log.d(TAG, "Calling tourRepository.getAllTours()...")
            val response = tourRepository.getAllTours(page = 0, size = _uiState.value.pageSize).first()
            Log.d(TAG, "Received response: ${response::class.simpleName}")
            when (response) {
                is Response.Loading -> {
                    Log.d(TAG, "Response is Loading")
                    _uiState.value = _uiState.value.copy(
                        toursResponse = Response.Loading as ToursResponse
                    )
                }
                is Response.Success -> {
                    val tourListResponse = response.data
                    Log.d(TAG, "Response is Success: ${tourListResponse.content.size} tours, hasMore: ${!tourListResponse.last}")
                    _uiState.value = _uiState.value.copy(
                        tours = tourListResponse.content,
                        currentPage = 0,
                        hasMore = !tourListResponse.last,
                        toursResponse = Response.Success(tourListResponse.content) as ToursResponse
                    )
                }
                is Response.Failure -> {
                    Log.e(TAG, "Response is Failure: ${response.e}")
                    _uiState.value = _uiState.value.copy(
                        toursResponse = Response.Failure(response.e) as ToursResponse
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in loadTours()", e)
            _uiState.value = _uiState.value.copy(
                toursResponse = Response.Failure(e.message ?: "Неизвестная ошибка") as ToursResponse
            )
        }
    }

    private fun searchTours() = viewModelScope.launch {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) {
            loadTours()
            return@launch
        }

        _uiState.value = _uiState.value.copy(
            currentPage = 0,
            hasMore = true,
            tours = emptyList(),
            toursResponse = Response.Loading as ToursResponse
        )

        val response = tourRepository.searchTours(
            destination = query,
            minPrice = null,
            maxPrice = null,
            page = 0,
            size = _uiState.value.pageSize
        ).first()
        
        when (response) {
            is Response.Loading -> {
                _uiState.value = _uiState.value.copy(
                    toursResponse = Response.Loading as ToursResponse
                )
            }
            is Response.Success -> {
                val tourListResponse = response.data
                _uiState.value = _uiState.value.copy(
                    tours = tourListResponse.content,
                    currentPage = 0,
                    hasMore = !tourListResponse.last,
                    toursResponse = Response.Success(tourListResponse.content) as ToursResponse
                )
            }
            is Response.Failure -> {
                _uiState.value = _uiState.value.copy(
                    toursResponse = Response.Failure(response.e) as ToursResponse
                )
            }
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

    private fun loadMoreTours() = viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState.isLoadingMore || !currentState.hasMore) {
            return@launch
        }

        _uiState.value = currentState.copy(isLoadingMore = true)
        
        val nextPage = currentState.currentPage + 1
        val query = currentState.searchQuery

        try {
            val response = if (query.isBlank()) {
                tourRepository.getAllTours(page = nextPage, size = currentState.pageSize).first()
            } else {
                tourRepository.searchTours(
                    destination = query,
                    minPrice = null,
                    maxPrice = null,
                    page = nextPage,
                    size = currentState.pageSize
                ).first()
            }

            when (response) {
                is Response.Success -> {
                    val tourListResponse = response.data
                    val newTours = currentState.tours + tourListResponse.content
                    _uiState.value = _uiState.value.copy(
                        tours = newTours,
                        currentPage = nextPage,
                        hasMore = !tourListResponse.last,
                        isLoadingMore = false,
                        toursResponse = Response.Success(newTours) as ToursResponse
                    )
                }
                is Response.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false
                    )
                }
                is Response.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoadingMore = false)
        }
    }

}
