package com.example.travelagency.presentation.view.favoritesScreen.uiEvent

sealed class FavoritesUiEvent {
    object OnRefresh : FavoritesUiEvent()
    data class OnRemoveFromFavorites(val tourId: Long) : FavoritesUiEvent()
}
