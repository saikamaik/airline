package com.example.travelagency.presentation.view.tourInfoScreen.uiEvent

sealed class TourInfoUiEvent {
    data object LoadFlights : TourInfoUiEvent()
    data object ToggleFavorite : TourInfoUiEvent()
}
