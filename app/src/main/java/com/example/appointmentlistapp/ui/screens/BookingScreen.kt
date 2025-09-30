package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R // <-- IMPORTANT: Ensure this import is correct for your project's resource R class
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel


// --- Helper Composable for Button (Optional, but cleaner) ---
@Composable
private fun ActionButton(config: ButtonConfig, viewModel: BookingViewModel) {
    Button(
        onClick = { viewModel.handleEvent(BookingEvent.ButtonClicked(config)) }
    ) {
        Icon(
            // Fetches the correct icon ID based on the string 'type'
            painter = painterResource(getIconForType(config.type.toString().trim())),
            contentDescription = config.text,
            // Standard size for icons in a button, with spacing at the end
            modifier = Modifier.size(14.dp).padding(end = 4.dp)
        )
        Text(config.text)
    }
}
// ---

@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()
    // Single source of truth for the UI State
    val state by bookingViewModel.uiState.collectAsState()

    // Fetches button configurations on screen launch
    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- HEADER SECTION (Title and Error) ---

        // FIX 1: Use Padding for layout separation, giving space at the bottom (16.dp)
        Text(
            text = "BookingScreen Content",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        state.errorMessage?.let { msg ->
            Text(
                text = "ERROR: $msg",
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        // FIX 2: Removed the redundant Spacer(modifier = Modifier.height(16.dp))
        // to minimize the large whitespace.

        // --- MASTER/DETAIL LAYOUT START ---
        Row(Modifier.fillMaxSize()) {

            // Master Pane Column (Buttons + List)
            Column(modifier = Modifier.weight(2f)) {

                // --- Dynamic Buttons Row ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.buttonConfigs.isNotEmpty()) {
                        state.buttonConfigs.forEach { config ->
                            ActionButton(config, bookingViewModel)
                        }
                    } else if (state.isLoading) {
                        Text(text = "Loading buttons...", modifier = Modifier.padding(8.dp))
                    } else {
                        Text(text = "No buttons configured.", modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Utility Toggle Button (Hardcoded Details Toggle) ---
                if (state.buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            // Creates a synthetic ButtonConfig event to toggle the details pane
                            bookingViewModel.handleEvent(BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    id = 0,
                                    clientId = "client123",
                                    screenId = "BookingScreen",
                                    buttonName = "DetailsToggle",
                                    action = "TOGGLE_DETAILS",
                                    type = "details",
                                    isVisible = 1,
                                    text = "Details",
                                    IconData = "" // Fixed: Replaced TODO() with empty string
                                )
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(if (state.showDetails) "Hide Details Pane" else "Show Details Pane")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Master Pane (List)
                BookingList(
                    bookings = state.bookings,
                    onBookingSelected = { booking -> bookingViewModel.handleEvent(BookingEvent.BookingSelected(booking)) },
                    onBookingCheckedChange = { bookingId -> bookingViewModel.handleEvent(BookingEvent.BookingCheckedChange(bookingId)) },
                    modifier = Modifier.fillMaxSize()
                )
            } // END Master Pane Column

            VerticalDivider()

            // Detail Pane
            if (state.showDetails) {
                BookingDetails(
                    booking = state.selectedBooking,
                    modifier = Modifier.weight(1f)
                )
            }
        } // END Master/Detail Row
    }
}