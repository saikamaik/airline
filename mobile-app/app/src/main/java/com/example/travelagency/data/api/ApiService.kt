package com.example.travelagency.data.api

import com.example.travelagency.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Tours
    @GET("tours")
    suspend fun getTours(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("destination") destination: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null
    ): Response<TourListResponse>

    @GET("tours/{id}")
    suspend fun getTourById(@Path("id") id: Long): Response<TourModel>

    @GET("tours/{id}/flights")
    suspend fun getTourFlights(@Path("id") tourId: Long): Response<List<FlightModel>>

    // Requests
    @POST("tours/{tourId}/request")
    suspend fun createRequest(
        @Path("tourId") tourId: Long,
        @Body request: ClientRequestModel
    ): Response<ClientRequestModel>

    @GET("client/requests")
    suspend fun getMyRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<RequestListResponse>

}
