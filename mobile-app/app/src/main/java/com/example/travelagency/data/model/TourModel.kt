package com.example.travelagency.data.model

import java.math.BigDecimal

data class TourModel(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
    val price: BigDecimal = BigDecimal.ZERO,
    val durationDays: Int = 0,
    val imageUrl: String? = null,
    val destinationCity: String = "",
    val active: Boolean = true,
    val flightIds: List<Int>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TourListResponse(
    val content: List<TourModel> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true,
    val first: Boolean = true,
    val numberOfElements: Int = 0
)
