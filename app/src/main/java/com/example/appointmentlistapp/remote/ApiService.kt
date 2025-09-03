package com.example.appointmentlistapp.data.remote

import com.example.appointmentlistapp.data.model.Appointment
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("api/appointments/driver/{driverId}")
    suspend fun getAppointmentsByDriverId(@Path("driverId") id: String): List<Appointment>

    @POST("api/appointments/{id}/checkin")
    suspend fun checkInAppointment(@Path("id") id: String): Response<Unit>
}