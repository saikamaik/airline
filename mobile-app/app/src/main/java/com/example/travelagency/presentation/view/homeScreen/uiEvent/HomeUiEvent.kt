package com.example.travelagency.presentation.view.homeScreen.uiEvent

sealed class HomeUiEvent {
    data class OnSearchQueryChange(val query: String) : HomeUiEvent()
    data object OnSearch : HomeUiEvent()
    data object OnRefresh : HomeUiEvent()
    data object LoadMoreTours : HomeUiEvent()
}
