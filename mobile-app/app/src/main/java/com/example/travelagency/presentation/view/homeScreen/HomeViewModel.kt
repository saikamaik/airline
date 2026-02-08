package com.example.travelagency.presentation.view.homeScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.TourRecommendation
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.domain.ToursResponse
import com.example.travelagency.domain.repository.RecommendationsRepository
import com.example.travelagency.presentation.view.homeScreen.uiEvent.HomeUiEvent
import com.example.travelagency.presentation.view.homeScreen.uiState.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tourRepository: TourRepository,
    private val recommendationsRepository: RecommendationsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<HomeUiState> = MutableStateFlow(HomeUiState())
    var uiState: StateFlow<HomeUiState> = _uiState

    private val _recommendations = MutableStateFlow<List<TourRecommendation>>(emptyList())
    val recommendations: StateFlow<List<TourRecommendation>> = _recommendations

    private val _isLoadingRecommendations = MutableStateFlow(false)
    val isLoadingRecommendations: StateFlow<Boolean> = _isLoadingRecommendations

    private var searchJob: Job? = null

    fun postUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnSearchQueryChange -> onSearchQueryChange(event.query)
            is HomeUiEvent.OnSearch -> searchTours()
            is HomeUiEvent.OnRefresh -> refreshTours()
            is HomeUiEvent.LoadMoreTours -> loadMoreTours()
            is HomeUiEvent.ToggleFilters -> toggleFilters()
            is HomeUiEvent.OnMinPriceChange -> onMinPriceChange(event.price)
            is HomeUiEvent.OnMaxPriceChange -> onMaxPriceChange(event.price)
            is HomeUiEvent.ApplyFilters -> applyFilters()
            is HomeUiEvent.ClearFilters -> clearFilters()
        }
    }

    init {
        Log.d(TAG, "========================================")
        Log.d(TAG, "HomeViewModel initialized, loading tours...")
        Log.d(TAG, "========================================")
        loadTours()
        loadRecommendations()
    }

    private fun loadRecommendations() = viewModelScope.launch {
        try {
            _isLoadingRecommendations.value = true
            Log.d(TAG, "Loading recommendations...")
            
            // Пока передаем null, так как у UserModel нет ID
            // В будущем можно добавить ID в модель или использовать username
            val result = recommendationsRepository.getRecommendations(userId = null, limit = 5)
            
            result.onSuccess { recs ->
                Log.d(TAG, "Successfully loaded ${recs.size} recommendations")
                _recommendations.value = recs
            }.onFailure { error ->
                Log.e(TAG, "Failed to load recommendations: ${error.message}")
                // Не показываем ошибку пользователю, просто не показываем рекомендации
                _recommendations.value = emptyList()
            }
        } finally {
            _isLoadingRecommendations.value = false
        }
    }

    private fun onSearchQueryChange(query: String) {
        Log.d(TAG, "Search query changed: $query")
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Отменяем предыдущий поиск
        searchJob?.cancel()
        
        // Запускаем новый поиск с задержкой
        searchJob = viewModelScope.launch {
            delay(500) // Задержка 500мс для debounce
            if (query.isBlank()) {
                loadTours() // Если запрос пустой, загружаем все туры
            } else {
                searchTours() // Иначе выполняем поиск
            }
        }
    }

    private fun loadTours() = viewModelScope.launch {
        Log.d(TAG, "========================================")
        Log.d(TAG, "loadTours() called, page: 0, size: ${_uiState.value.pageSize}")
        Log.d(TAG, "========================================")
        _uiState.value = _uiState.value.copy(
            currentPage = 0,
            hasMore = true,
            tours = emptyList(),
            toursResponse = Response.Loading as ToursResponse
        )
        
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
    }

    private fun searchTours() = viewModelScope.launch {
        val query = _uiState.value.searchQuery
        val minPrice = _uiState.value.minPrice
        val maxPrice = _uiState.value.maxPrice
        
        if (query.isBlank() && minPrice == null && maxPrice == null) {
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
            destination = query.ifBlank { null },
            minPrice = minPrice,
            maxPrice = maxPrice,
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

    private fun toggleFilters() {
        _uiState.value = _uiState.value.copy(
            showFilters = !_uiState.value.showFilters
        )
    }

    private fun onMinPriceChange(price: Double?) {
        _uiState.value = _uiState.value.copy(minPrice = price)
    }

    private fun onMaxPriceChange(price: Double?) {
        _uiState.value = _uiState.value.copy(maxPrice = price)
    }

    private fun applyFilters() {
        Log.d(TAG, "Applying filters: minPrice=${_uiState.value.minPrice}, maxPrice=${_uiState.value.maxPrice}")
        searchTours()
    }

    private fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            minPrice = null,
            maxPrice = null,
            searchQuery = ""
        )
        loadTours()
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
        val minPrice = currentState.minPrice
        val maxPrice = currentState.maxPrice

        try {
            val response = if (query.isBlank() && minPrice == null && maxPrice == null) {
                tourRepository.getAllTours(page = nextPage, size = currentState.pageSize).first()
            } else {
                tourRepository.searchTours(
                    destination = query.ifBlank { null },
                    minPrice = minPrice,
                    maxPrice = maxPrice,
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
