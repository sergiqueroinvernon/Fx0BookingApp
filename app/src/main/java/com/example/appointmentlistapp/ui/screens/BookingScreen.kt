package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.AppointmentViewModel
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.ui.viewmodel.BookingViewModel

@Composable
fun BookingScreen(viewModel: BookingViewModel = viewModel()) {
    val bookings by viewModel.bookings.collectAsState()
    val selectedBooking by viewModel.selectedBooking.collectAsState()

    Row(Modifier.fillMaxSize()) {
        // Master Pane (List)
        BookingList(
            bookings = bookings,
            onBookingSelected = { booking ->
                viewModel.selectBooking(booking)
            },
            modifier = Modifier.weight(1f) // Takes 1/3 of the space
        )

        VerticalDivider()

        // Detail Pane
        BookingDetails(
            booking = selectedBooking,
            modifier = Modifier.weight(1.5f) // Takes 2/3 of the space
        )
    }
}