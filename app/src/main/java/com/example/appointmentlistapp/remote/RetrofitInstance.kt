package com.example.appointmentlistapp.data.remote

import com.example.appointmentlistapp.data.BookingAppService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {

    private const val BASE_URL = "http://127.0.0.1:5251/"
    private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                // 💡 You would add an OkHttpClient here if you needed logging, etc.
                .build()
        }

        val api: BookingAppService by lazy {
            retrofit.create(BookingAppService::class.java) // Correctly creates the service
        }

    }