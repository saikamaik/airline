package com.example.travelagency.data.repositoryImplementation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.model.AuthResponse
import com.example.travelagency.data.model.ErrorResponse
import com.example.travelagency.data.model.LoginRequest
import com.example.travelagency.data.model.RegisterRequest
import com.example.travelagency.data.model.Response
import com.example.travelagency.data.model.UserModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
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

    companion object {
        private const val TAG = "AuthRepository"
        private const val PREFS_NAME = "auth_prefs"
        
        // Получение EncryptedSharedPreferences для безопасного хранения токенов
        fun getEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    private val prefs: SharedPreferences = getEncryptedPrefs(context)

    override fun signIn(username: String, password: String): Flow<SignInResponse> = flow {
        emit(Response.Loading)

        try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveToken(authResponse.token, authResponse.username)
                Log.d(TAG, "User signed in successfully: $username")
                emit(Response.Success(authResponse))
            } else {
                val errorMsg = "Неверный логин или пароль"
                Log.w(TAG, "Sign in failed: ${response.code()} - ${response.message()}")
                emit(Response.Failure(e = errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Ошибка подключения к серверу"
            Log.e(TAG, "Sign in error", e)
            emit(Response.Failure(e = errorMsg))
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
                Log.d(TAG, "User registered successfully: $username")
                emit(Response.Success(authResponse))
            } else {
                // Парсим ошибку из JSON ответа
                val errorMsg = parseErrorResponse(response.errorBody()?.string())
                    ?: "Ошибка регистрации"
                Log.w(TAG, "Registration failed: ${response.code()} - $errorMsg")
                emit(Response.Failure(e = errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = e.message ?: "Ошибка подключения к серверу"
            Log.e(TAG, "Registration error", e)
            emit(Response.Failure(e = errorMsg))
        }
    }

    override fun getCurrentUser(): UserModel? {
        val token = getToken()
        val username = prefs.getString("username", null)
        return if (token != null && username != null) {
            UserModel(username = username, token = token)
        } else {
            Log.d(TAG, "No current user found")
            null
        }
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

    /**
     * Парсит JSON ответ с ошибкой от сервера
     */
    private fun parseErrorResponse(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        
        return try {
            val gson = Gson()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.error ?: errorResponse.message
        } catch (e: JsonSyntaxException) {
            Log.w(TAG, "Failed to parse error response: $errorBody", e)
            null
        }
    }

}
