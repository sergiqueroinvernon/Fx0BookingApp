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
import com.example.appointmentlistapp.data.components.ButtonConfig // RETAINED for type compatibility
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel


@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()
    val state by bookingViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "BookingScreen Content", modifier = Modifier.padding(16.dp))

        state.errorMessage?.let { msg ->
            Text(text = "ERROR: $msg", color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- MASTER/DETAIL LAYOUT START ---
        Row(Modifier.fillMaxSize()) {

            // Master Pane Column (List + Buttons)
            Column(modifier = Modifier.weight(2f)) {

                // --- Dynamic Buttons Row (Data-Driven from buttonConfigs) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.buttonConfigs.isNotEmpty()) {
                        state.buttonConfigs.forEach { config ->
                            // Assuming 'config' is now treated as a dynamic map (or a generic data model wrapper)

                            // We assume the ViewModel returns ButtonConfig for this example
                            // to avoid deeper refactoring into the data layer and ViewModel.
                            // If the data layer returned a Map<String, Any>, you'd use that here:
                            // val type = config["type"] as? String ?: ""
                            // val text = config["text"] as? String ?: "Action"

                            Button(
                                onClick = { bookingViewModel.handleEvent(BookingEvent.ButtonClicked(config)) }
                            ) {
                                // Fix: Increased icon size from 10.dp (tiny) to 24.dp (standard)
                                Icon(
                                    painter = painterResource(getIconForType(config.type.toString().trim())),
                                    contentDescription = config.text,
                                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                                )
                                Text(config.text)
                            }
                        }
                    } else if (state.isLoading) {
                        Text(text = "Loading buttons...", modifier = Modifier.padding(horizontal = 8.dp))
                    } else {
                        Text(text = "No buttons configured.", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Utility Toggle Button (Fixing the NotImplementedError) ---
                if (state.buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            // FIX: Replaced TODO() with an empty string "" to avoid NotImplementedError.
                            // This ensures the button works and sends a valid event.
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
                                    IconData = ""
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