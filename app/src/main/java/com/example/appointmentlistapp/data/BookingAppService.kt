package com.example.appointmentlistapp.data

import androidx.camera.core.ImageProcessor
import com.example.appointmentlistapp.data.api.BookingApiModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface BookingAppService {

    @GET("api/appointments")
    suspend fun getAppointments(): List<BookingApiModel>
    @GET("api/appointments/driver/{driverId}")
    suspend fun getAppointmentsByDriverId(@Path("driverId") driverId: String): List<BookingApiModel>
    @POST("api/appointments/{id}/checkin")
    suspend fun checkInAppointment(@Path("id") id: String): Response<Void>
    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: BookingApiModel): BookingApiModel
    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body appointment: BookingApiModel): BookingApiModel

}