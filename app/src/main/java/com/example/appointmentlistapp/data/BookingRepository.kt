package com.example.appointmentlistapp.data

import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.Flow

//Repository -> Provide/Transform the data to the ViewModel. Intermediary between UI and Data Sources./Interact with the Networking Service
import com.example.appointmentlistapp.data.api.BookingApiModel
import kotlinx.coroutines.flow.flow

// Repository -> Intermediary between UI and Data Sources. Now using Networking Service.
class BookingRepository(private val bookingAppService: BookingAppService) {

    fun getAppointments(): Flow<List<BookingApiModel>> = flow {
        // Here, we call the API service and emit the result
        val appointments: List<BookingApiModel> = bookingAppService.getAppointments()
        emit(appointments)
    }

    fun getButtonsForClientsAndScreen(clientId: String, screenId: String): Flow<List<ButtonConfig>> =
        flow {
            val buttonConfigs: List<ButtonConfig> = bookingAppService.getButtonsForClientAndScreen(clientId, screenId)
            emit(buttonConfigs)


        }

    suspend fun getAppointmentsByDriver(driverId: String): List<BookingApiModel> {
        // This function doesn't use a Flow because the data is a one-time request
        return bookingAppService.getAppointmentsByDriverId(driverId)
    }

    suspend fun checkInAppointment(id: String): Boolean {
        return bookingAppService.checkInAppointment(id).isSuccessful
    }

    suspend fun createAppointment(appointment: Booking) {
        bookingAppService.createAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Booking) {
        bookingAppService.deleteAppointment(appointment.bookingId)
    }

    suspend fun updateAppointment(id: String, appointment: BookingApiModel) {
        bookingAppService.updateAppointment(id, appointment)
    }
}