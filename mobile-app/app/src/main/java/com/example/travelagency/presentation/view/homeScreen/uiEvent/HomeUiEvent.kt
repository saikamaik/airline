package com.example.travelagency.presentation.view.homeScreen.uiEvent

sealed class HomeUiEvent {
    data class OnSearchQueryChange(val query: String) : HomeUiEvent()
    data object OnSearch : HomeUiEvent()
    data object OnRefresh : HomeUiEvent()
    data object LoadMoreTours : HomeUiEvent()
    data object ToggleFilters : HomeUiEvent()
    data class OnMinPriceChange(val price: Double?) : HomeUiEvent()
    data class OnMaxPriceChange(val price: Double?) : HomeUiEvent()
    data object ApplyFilters : HomeUiEvent()
    data object ClearFilters : HomeUiEvent()
}
