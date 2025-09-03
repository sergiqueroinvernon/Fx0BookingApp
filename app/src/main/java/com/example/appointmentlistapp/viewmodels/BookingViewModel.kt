package com.example.appointmentlistapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appointmentlistapp.data.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel: ViewModel() {

    private val _bookings = MutableStateFlow(
        listOf(
            Booking(
                "10001",
                "D-EM 711E",
                "20.02.2025",
                "Baustelle",
                "Details für Fahrt zur Baustelle A40..."
            ),
            Booking("10002", "SWD-HW 200", "13.02.2025", "Dienstreise", "Details für Dienstreise nach Köln..."),
            Booking("10003", "D-WG 450E", "06.02.2025", "Baustelle", "Details für Fahrt zur Baustelle B1...")
        )

    )

    val bookings: StateFlow<List<Booking>> = _bookings

    //This will hold the booking that the user clicks on
    private val _selectedBooking = MutableStateFlow<Booking?>(null)

    // This function is called when a user clicks an item in the list
    fun selectBooking(booking: Booking) {
        _selectedBooking.value = booking
    }



}