package com.example.appointmentlistapp.data

// In app/src/test/java/com/example/appointmentlistapp/viewmodels/FakeBookingRepository.kt


import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow

class FakeBookingRepository(bookingAppService: BookingAppService) :
    BookingRepository(bookingAppService) {
    // We'll use a MutableStateFlow to simulate a real Flow from the repository
    private val _appointmentsFlow = MutableStateFlow(emptyList<Appointment>())

    // This is a simplified way to represent our data.
    private val bookings = mutableListOf<Booking>()

    // Use a flow to emit the current list of appointments
    override fun getAppointments(): Flow<List<Appointment>> {
        return _appointmentsFlow
    }

    override fun getButtonsForClientAndScreen(clientId: String, screenId: String): Flow<List<ButtonConfig>> {
        // Return a dummy flow for testing purposes
        return flow { emit(emptyList()) }
    }

    override suspend fun getAppointmentsByDriver(driverId: String): List<Appointment> {
        // Return a dummy list or specific test data
        return emptyList()
    }

    override suspend fun createAppointment(appointment: Booking) {
        bookings.add(appointment)
        // You can add more logic here if needed for specific tests
    }

    override suspend fun deleteAppointment(booking: Booking) {
        bookings.remove(booking)
    }

    // A helper function to control the data returned by the repository during tests
    fun setAppointments(list: List<Appointment>) {
        _appointmentsFlow.value = list
    }
}