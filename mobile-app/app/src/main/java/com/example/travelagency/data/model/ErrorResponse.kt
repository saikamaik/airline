package com.example.travelagency.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("message")
    val message: String? = null
)
