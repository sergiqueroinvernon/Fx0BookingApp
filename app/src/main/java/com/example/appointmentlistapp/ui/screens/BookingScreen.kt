package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel
import com.example.appointmentlistapp.ui.components.AppointmentItem
import com.example.appointmentlistapp.ui.components.BookingItem


// --- Helper Composable for Button (Optional, but cleaner) ---
@Composable
private fun ActionButton(
    config: ButtonConfig,
    viewModel: BookingViewModel,
    modifier: Modifier = Modifier) {
    Button(
        onClick = { viewModel.handleEvent(BookingEvent.ButtonClicked(config)) },
        modifier = modifier
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
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                ) {
                    if (state.buttonConfigs.isNotEmpty()) {
                        state.buttonConfigs.forEach { config ->
                            ActionButton(
                                config,
                                bookingViewModel,
                                modifier = Modifier.width(160.dp)

                            )
                        }
                    } else if (state.isLoading) {
                        Text(text = "Loading buttons...", modifier = Modifier.padding(8.dp))
                    } else {
                        Text(text = "No buttons configured.", modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Utility Toggle Button (Hardcoded Details Toggle) ---
                // This button is only shown if there isn't a "details" button from the backend
                // and there's a selected booking to show details for.
                if (state.selectedBooking != null && state.buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                Button(
                    onClick = {
                        // Creates a synthetic ButtonConfig event to toggle the details pane
                        bookingViewModel.handleEvent(
                            BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    id = 0, // Synthetic ID
                                    clientId = "client123",
                                    screenId = "BookingScreen",
                                    buttonName = "DetailsToggle",
                                    action = "TOGGLE_DETAILS",
                                    type = "details", // Critical: This type triggers the toggle in ViewModel
                                    isVisible = 1,
                                    text = if (state.showDetails) "Hide Details" else "Show Details",
                                    IconData = TODO()
                                )
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(if (state.showDetails) "Hide Details Pane" else "Show Details Pane")
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        state.bookings,
                        key = { booking -> booking.bookingId }
                    ) { appointment ->
                        BookingItem(
                            booking = appointment,
                            isChecked = appointment.isChecked,
                            onCheckedChange = {
                                bookingViewModel.handleEvent(
                                    BookingEvent.BookingCheckedChange(
                                        appointment.bookingId
                                    )
                                )
                            },

                            )
                    }

                }
            }

            // Detail Pane (conditionally displayed on the right)
            if (state.showDetails ) {
                VerticalDivider(modifier = Modifier.fillMaxHeight().width(8.dp))
               // Visually separates the master and detail panes
                Box(modifier = Modifier.weight(1f).padding(16.dp)) { // Take remaining space
                    BookingDetails(booking = state.selectedBooking)
                }
            }
        } // END Master/Detail Row
    }
}