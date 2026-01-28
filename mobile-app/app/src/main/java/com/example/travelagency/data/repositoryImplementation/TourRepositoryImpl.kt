package com.example.travelagency.data.repositoryImplementation

import android.util.Log
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.model.ClientRequestModel
import com.example.travelagency.data.model.Response
import com.example.travelagency.domain.CreateRequestResponse
import com.example.travelagency.domain.FlightsResponse
import com.example.travelagency.domain.PaginatedToursResponse
import com.example.travelagency.domain.RequestsResponse
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.domain.TourResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

private const val TAG = "TourRepository"

class TourRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TourRepository {

    override fun getAllTours(page: Int, size: Int): Flow<PaginatedToursResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getTours(page = page, size = size)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Failed to load tours: ${response.code()} - ${response.message()}")
                emit(Response.Failure(e = "Ошибка загрузки туров"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tours", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getTourById(id: Long): Flow<TourResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getTourById(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Tour not found: id=$id, code=${response.code()}")
                emit(Response.Failure(e = "Тур не найден"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tour: id=$id", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getTourFlights(tourId: Long): Flow<FlightsResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getTourFlights(tourId)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Flights not found: tourId=$tourId, code=${response.code()}")
                emit(Response.Failure(e = "Рейсы не найдены"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flights: tourId=$tourId", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun searchTours(
        destination: String?,
        minPrice: Double?,
        maxPrice: Double?,
        page: Int,
        size: Int
    ): Flow<PaginatedToursResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getTours(
                page = page,
                size = size,
                destination = destination,
                minPrice = minPrice,
                maxPrice = maxPrice
            )
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Search failed: destination=$destination, code=${response.code()}")
                emit(Response.Failure(e = "Ошибка поиска"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching tours: destination=$destination", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun createRequest(
        tourId: Long,
        userName: String,
        userEmail: String,
        userPhone: String?,
        comment: String?
    ): Flow<CreateRequestResponse> = flow {
        emit(Response.Loading)

        try {
            val request = ClientRequestModel(
                tourId = tourId,
                userName = userName,
                userEmail = userEmail,
                userPhone = userPhone,
                comment = comment
            )
            val response = apiService.createRequest(tourId, request)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Request created successfully: tourId=$tourId")
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Failed to create request: tourId=$tourId, code=${response.code()}")
                emit(Response.Failure(e = "Ошибка создания заявки"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating request: tourId=$tourId", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getMyRequests(page: Int, size: Int): Flow<RequestsResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.getMyRequests(page, size)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!.content))
            } else {
                Log.w(TAG, "Failed to load requests: code=${response.code()}")
                emit(Response.Failure(e = "Ошибка загрузки заявок"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading requests", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

}
