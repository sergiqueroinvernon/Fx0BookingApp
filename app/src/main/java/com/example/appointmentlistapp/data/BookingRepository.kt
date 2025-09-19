package com.example.appointmentlistapp.data

import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.Flow

//Repository -> Provide/Transform the data to the ViewModel. Intermediary between UI and Data Sources./Interact with the Networking Service
import com.example.appointmentlistapp.data.BookingAppService
import com.example.appointmentlistapp.data.api.BookingApiModel
import kotlinx.coroutines.flow.flow

// Repository -> Intermediary between UI and Data Sources. Now using Networking Service.
class BookingRepository(private val bookingAppService: BookingAppService) {

    fun getAppointments(): Flow<List<BookingApiModel>> = flow {
        // Here, we call the API service and emit the result
        val appointments: List<BookingApiModel> = bookingAppService.getAppointments()
        emit(appointments)
    }

    suspend fun getAppointmentsByDriver(driverId: String): List<BookingApiModel> {
        // This function doesn't use a Flow because the data is a one-time request
        return bookingAppService.getAppointmentsByDriverId(driverId)
    }

    suspend fun checkInAppointment(id: String): Boolean {
        return bookingAppService.checkInAppointment(id).isSuccessful
    }

    suspend fun createAppointment(appointment: BookingApiModel) {
        bookingAppService.createAppointment(appointment)
    }

    suspend fun updateAppointment(id: String, appointment: BookingApiModel) {
        bookingAppService.updateAppointment(id, appointment)
    }
}