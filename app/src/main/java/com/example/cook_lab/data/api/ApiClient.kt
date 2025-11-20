package com.example.cook_lab.data.api

import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
//    const val BASE_URL = "http://172.20.10.2:8080/"
    const val BASE_URL = "http://192.168.88.157:8080/"

    val apiService: ApiService by lazy {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Tạo Logging Interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request & response body
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)      // Thêm logging interceptor
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                Prefs.token?.let { token ->
                    Log.d("ApiClient", "Adding Authorization header with token: $token")
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout( 20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    fun api(): ApiService = apiService
}
