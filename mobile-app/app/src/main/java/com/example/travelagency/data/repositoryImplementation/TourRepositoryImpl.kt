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
        Log.d(TAG, "========================================")
        Log.d(TAG, "getAllTours() called: page=$page, size=$size")
        Log.d(TAG, "========================================")
        // НЕ emit(Response.Loading) здесь - иначе .first() отменит Flow!

        try {
            Log.d(TAG, "Calling apiService.getTours()...")
            val response = apiService.getTours(page = page, size = size)
            Log.d(TAG, "API response received: code=${response.code()}, isSuccessful=${response.isSuccessful}, body=${response.body() != null}")
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "Tours loaded successfully: ${body.content.size} tours, total=${body.totalElements}, last=${body.last}")
                emit(Response.Success(body))
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Failed to load tours: code=${response.code()}, message=${response.message()}, errorBody=$errorBody")
                emit(Response.Failure(e = "Ошибка загрузки туров: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Не ловим CancellationException (включая AbortFlowException от .first())
            if (e is kotlinx.coroutines.CancellationException) {
                Log.d(TAG, "Flow cancelled (normal behavior for .first())")
                throw e
            }
            Log.e(TAG, "Exception in getAllTours()", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getTourById(id: Long): Flow<TourResponse> = flow {
        // НЕ emit(Response.Loading) - иначе .first() отменит Flow!
        
        try {
            val response = apiService.getTourById(id)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Tour not found: id=$id, code=${response.code()}")
                emit(Response.Failure(e = "Тур не найден"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error loading tour: id=$id", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getTourFlights(tourId: Long): Flow<FlightsResponse> = flow {
        // НЕ emit(Response.Loading) - иначе .first() отменит Flow!
        
        try {
            val response = apiService.getTourFlights(tourId)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!))
            } else {
                Log.w(TAG, "Flights not found: tourId=$tourId, code=${response.code()}")
                emit(Response.Failure(e = "Рейсы не найдены"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
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
        // НЕ emit(Response.Loading) - иначе .first() отменит Flow!
        
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
            if (e is kotlinx.coroutines.CancellationException) throw e
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
        // НЕ emit(Response.Loading) - иначе .first() отменит Flow!
        
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
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error creating request: tourId=$tourId", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

    override fun getMyRequests(page: Int, size: Int): Flow<RequestsResponse> = flow {
        // НЕ emit(Response.Loading) - иначе .first() отменит Flow!
        
        try {
            val response = apiService.getMyRequests(page, size)
            if (response.isSuccessful && response.body() != null) {
                emit(Response.Success(response.body()!!.content))
            } else {
                Log.w(TAG, "Failed to load requests: code=${response.code()}")
                emit(Response.Failure(e = "Ошибка загрузки заявок"))
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "Error loading requests", e)
            emit(Response.Failure(e = e.message ?: "Ошибка подключения"))
        }
    }

}
