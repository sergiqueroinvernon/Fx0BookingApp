package com.example.appointmentlistapp.data

import com.example.appointmentlistapp.data.api.BookingApiModel
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface BookingAppService {

    @GET("api/buttons/{clientId}/{screenId}")
    suspend fun getButtonsForClientAndScreen(
        @Path("clientId") clientId: String,
        @Path("screenId") screenId: String
    ): List<ButtonConfig>
    @GET("api/appointments")
    suspend fun getAppointments(): List<Appointment>
    @GET("api/appointments/driver/{driverId}")
    suspend fun getAppointmentsByDriverId(@Path("driverId") driverId: String): List<Appointment>
    @POST("api/appointments/{id}/checkin")
    suspend fun checkInAppointment(@Path("id") id: String): Response<Void>
    @POST("api/appointments")
    suspend fun createAppointment(@Body appointment: Booking): Appointment
    @PUT("api/appointments/{id}")
    suspend fun updateAppointment(@Path("id") id: String, @Body appointment: Appointment): Appointment

    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: String?): Response<Void>




    companion object

}