package com.example.appointmentlistapp.data.remote

import com.example.appointmentlistapp.data.BookingAppService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "http://172.26.140.23:5251/"

object RetrofitInstance {
    val api: BookingAppService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookingAppService::class.java)
    }
}