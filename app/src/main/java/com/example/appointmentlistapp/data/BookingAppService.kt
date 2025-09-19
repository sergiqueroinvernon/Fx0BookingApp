package com.example.appointmentlistapp.data

interface BookingAppService {

    @Get("api/appointments")
    suspend fun getAppointments(): List<Booking>
}