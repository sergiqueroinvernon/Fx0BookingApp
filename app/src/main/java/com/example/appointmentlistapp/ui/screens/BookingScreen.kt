package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.ui.viewmodel.LogBookViewModel
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingViewModel
@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()
    val bookings by bookingViewModel.bookings.collectAsState()
    val selectedBooking by bookingViewModel.selectedBooking.collectAsState()
    var showDetails by remember { mutableStateOf(true) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val buttonConfigs by bookingViewModel.buttonsConfig.collectAsState()


    Column {
        Text(text = "BookingScreen Content")
        Spacer(modifier = Modifier.height(16.dp))

        if (buttonConfigs.isNotEmpty()) {
            //Iterate through all the items
            buttonConfigs.forEach { config ->
                Button(onClick = { showDetails = !showDetails }) {
                    Icon(
                        painter = painterResource(getIconForType(config.type)),
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp).padding(end = 4.dp)
                    )
                    // Change button text based on the state
                    Text(if (showDetails) "Hide Details" else "Show Details")
                }
            }
        }

        Button(onClick = {
            // Example: Update testResult with some data from the ViewModel
            testResult = bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen").toString()
        }) {
            Text("Show Test Result")
        }
        testResult?.let {
            Text(text = "Test Result: $it")
        }

        Row(Modifier.fillMaxSize()) {
            // Master Pane (List)
            BookingList(
                bookings = bookings,
                onBookingSelected = { booking ->
                    bookingViewModel.selectBooking(booking)
                },
                onBookingCheckedChange = { bookingId ->
                    bookingViewModel.toggleBookingChecked(bookingId)
                },
                modifier = Modifier.weight(2f) // Give more space to the list
            )

            VerticalDivider()

            if (showDetails) {// Detail Pane
                BookingDetails(
                    booking = selectedBooking,
                    modifier = Modifier.weight(1f) // Give less space to details
                )
            }
        }
    }
}