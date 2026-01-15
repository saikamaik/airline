package com.example.travelagency.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class AuthResponse(
    val token: String = "",
    val username: String = "",
    val roles: List<String> = emptyList()
)

data class UserModel(
    val username: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = null,
    val token: String = ""
)
