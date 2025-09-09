package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.ui.viewmodel.BookingViewModel

@Composable
fun BookingScreen(viewModel: BookingViewModel = viewModel()) {
    val bookings by viewModel.bookings.collectAsState()
    val selectedBooking by viewModel.selectedBooking.collectAsState()
    var showDetails by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier =  Modifier.fillMaxWidth()){


            Button(onClick = { showDetails = !showDetails}) { // <-- Toggle the state here
                // Change button text based on the state
                Text("Details")
            }
        }
    Row(Modifier.fillMaxSize()) {
        // Master Pane (List)
        BookingList(
            bookings = bookings,
            onBookingSelected = { booking ->
                viewModel.selectBooking(booking)
            },
            onBookingCheckedChange = { bookingId ->
                viewModel.toggleBookingChecked(bookingId)
            },
            modifier = Modifier.weight(2f) // Give more space to the list
        )

        VerticalDivider()

        if(showDetails)
        {// Detail Pane
        BookingDetails(
            booking = selectedBooking,
            modifier = Modifier.weight(1f) // Give less space to details
        )}
    }
}}