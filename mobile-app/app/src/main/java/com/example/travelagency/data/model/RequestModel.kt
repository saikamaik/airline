package com.example.travelagency.data.model

data class ClientRequestModel(
    val id: Long? = null,
    val tourId: Long = 0,
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String? = null,
    val comment: String? = null,
    val status: String? = null,
    val priority: String? = null,
    val createdAt: String? = null,
    val tourName: String? = null
)

data class RequestListResponse(
    val content: List<ClientRequestModel> = emptyList(),
    val totalElements: Int = 0,
    val totalPages: Int = 0,
    val last: Boolean = true,
    val first: Boolean = true
)
