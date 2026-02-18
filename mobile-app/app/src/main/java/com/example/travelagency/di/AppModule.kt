package com.example.travelagency.di

import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.ImageRequest
import coil.request.Options
import coil.util.DebugLogger
import com.example.travelagency.BuildConfig
import com.example.travelagency.data.api.ApiService
import com.example.travelagency.data.repositoryImplementation.AuthRepositoryImpl
import com.example.travelagency.data.repositoryImplementation.TourRepositoryImpl
import com.example.travelagency.data.repositoryImplementation.RecommendationsRepositoryImpl
import com.example.travelagency.domain.AuthRepository
import com.example.travelagency.domain.TourRepository
import com.example.travelagency.domain.repository.RecommendationsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Используем BASE_URL из BuildConfig (можно переопределить в build.gradle.kts)
    private val BASE_URL = BuildConfig.BASE_URL

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): Interceptor {
        return Interceptor { chain ->
            // Используем EncryptedSharedPreferences для безопасного хранения токена
            val prefs = AuthRepositoryImpl.getEncryptedPrefs(context)
            val token = prefs.getString("token", null)

            val originalRequest = chain.request()
            android.util.Log.d("AuthInterceptor", "Request: ${originalRequest.method} ${originalRequest.url}")
            android.util.Log.d("AuthInterceptor", "Token present: ${token != null}")

            val request = originalRequest.newBuilder()
            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
                android.util.Log.d("AuthInterceptor", "Added Authorization header")
            }
            
            val response = chain.proceed(request.build())
            android.util.Log.d("AuthInterceptor", "Response: ${response.code} ${response.message}")
            response
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: Interceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Всегда включаем логирование для диагностики
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        android.util.Log.d("AppModule", "Initializing Retrofit with BASE_URL: $BASE_URL")
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepositoryImpl(apiService, context)

    @Provides
    @Singleton
    fun provideTourRepository(apiService: ApiService): TourRepository =
        TourRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideFavoritesRepository(apiService: ApiService): com.example.travelagency.domain.FavoritesRepository =
        com.example.travelagency.data.repositoryImplementation.FavoritesRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideRecommendationsRepository(apiService: ApiService): RecommendationsRepository =
        RecommendationsRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .logger(DebugLogger(Log.VERBOSE))
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }

}
