package com.example.travelagency.domain

import com.example.travelagency.data.model.AuthResponse
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.UserModel
import kotlinx.coroutines.flow.Flow

typealias SignInResponse = Response<AuthResponse>
typealias SignUpResponse = Response<AuthResponse>

interface AuthRepository {

    fun signIn(username: String, password: String): Flow<SignInResponse>

    fun signUp(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Flow<SignUpResponse>

    fun getCurrentUser(): UserModel?

    fun saveToken(token: String, username: String)

    fun getToken(): String?

    fun logout()

    fun isLoggedIn(): Boolean

}
