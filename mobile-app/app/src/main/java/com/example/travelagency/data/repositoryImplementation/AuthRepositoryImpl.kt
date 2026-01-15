package com.example.travelagency.data.repositoryImplementation

import android.content.Context
import android.content.SharedPreferences
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.model.AuthResponse
import com.example.travelagency.data.model.LoginRequest
import com.example.travelagency.data.model.RegisterRequest
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.UserModel
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.domain.SignInResponse
import com.example.travelagency.domain.SignUpResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext val context: Context
) : AuthRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override fun signIn(username: String, password: String): Flow<SignInResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveToken(authResponse.token, authResponse.username)
                emit(Response.Success(authResponse))
            } else {
                emit(Response.Failure(e = "Неверный логин или пароль"))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e = e.message ?: "Ошибка подключения к серверу"))
        }
    }

    override fun signUp(
        username: String,
        password: String,
        email: String,
        firstName: String,
        lastName: String,
        phone: String?
    ): Flow<SignUpResponse> = flow {
        emit(Response.Loading)

        try {
            val request = RegisterRequest(
                username = username,
                password = password,
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone
            )
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveToken(authResponse.token, authResponse.username)
                emit(Response.Success(authResponse))
            } else {
                emit(Response.Failure(e = "Ошибка регистрации"))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e = e.message ?: "Ошибка подключения к серверу"))
        }
    }

    override fun getCurrentUser(): UserModel? {
        val token = getToken()
        val username = prefs.getString("username", null)
        return if (token != null && username != null) {
            UserModel(username = username, token = token)
        } else null
    }

    override fun saveToken(token: String, username: String) {
        prefs.edit()
            .putString("token", token)
            .putString("username", username)
            .apply()
    }

    override fun getToken(): String? {
        return prefs.getString("token", null)
    }

    override fun logout() {
        prefs.edit().clear().apply()
    }

    override fun isLoggedIn(): Boolean {
        return getToken() != null
    }

}
