package com.example.travelagency.data.model

data class FlightModel(
    val flightId: Int? = null,
    val flightNo: String = "",
    val scheduledDeparture: String = "",
    val scheduledArrival: String = "",
    val departureAirportCode: String = "",
    val arrivalAirportCode: String = "",
    val status: String = "",
    val aircraftCode: String? = null,
    val actualDeparture: String? = null,
    val actualArrival: String? = null
)
