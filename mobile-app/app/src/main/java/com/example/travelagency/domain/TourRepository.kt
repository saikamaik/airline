package com.example.travelagency.domain

import com.example.travelagency.data.model.ClientRequestModel
import com.example.travelagency.data.model.FlightModel
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.TourListResponse
import com.example.travelagency.data.model.TourModel
import kotlinx.coroutines.flow.Flow

typealias Tours = List<TourModel>
typealias ToursResponse = Response<Tours>
typealias PaginatedToursResponse = Response<TourListResponse>
typealias TourResponse = Response<TourModel>
typealias FlightsResponse = Response<List<FlightModel>>
typealias CreateRequestResponse = Response<ClientRequestModel>
typealias RequestsResponse = Response<List<ClientRequestModel>>

interface TourRepository {

    fun getAllTours(page: Int, size: Int): Flow<PaginatedToursResponse>

    fun getTourById(id: Long): Flow<TourResponse>

    fun getTourFlights(tourId: Long): Flow<FlightsResponse>

    fun searchTours(
        destination: String?,
        minPrice: Double?,
        maxPrice: Double?,
        page: Int,
        size: Int
    ): Flow<PaginatedToursResponse>

    fun createRequest(
        tourId: Long,
        userName: String,
        userEmail: String,
        userPhone: String?,
        comment: String?
    ): Flow<CreateRequestResponse>

    fun getMyRequests(page: Int, size: Int): Flow<RequestsResponse>

}
