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
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.ui.components.BookingList
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel


@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()

    // 💡 COLLECT ONLY THE CONSOLIDATED UI STATE
    val state by bookingViewModel.uiState.collectAsState()

    // Trigger API call once when the screen is first created
    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "BookingScreen Content")
        // 💡 Display Error Message if present
        state.errorMessage?.let { msg ->
            Text(text = "ERROR: $msg", color = Color.Red)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- MASTER/DETAIL LAYOUT START ---
        Row(Modifier.fillMaxSize()) {

            // Master Pane Column (List + Buttons)
            Column(modifier = Modifier.weight(2f)) {

                // --- Dynamic Buttons Row (Now fully data-driven) ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.buttonConfigs.isNotEmpty()) {
                        state.buttonConfigs.forEach { config ->

                            Button(
                                // 💡 SEND EVENT TO VIEWMODEL
                                onClick = { bookingViewModel.handleEvent(BookingEvent.ButtonClicked(config)) }
                            ) {
                                Icon(
                                    painter = painterResource(getIconForType(config.type.toString().trim())),
                                    contentDescription = config.text,
                                    modifier = Modifier.size(15.dp).padding(end = 4.dp)
                                )
                                Text(config.text)
                            }
                        }
                    } else if (state.isLoading) {
                        Text(text = "Loading buttons...")
                    } else {
                        Text(text = "No buttons configured.")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Utility Toggle Button (Only show if no 'details' button from API)
                if (state.buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            // 💡 Send event to toggle, managed by the ViewModel
                            bookingViewModel.handleEvent(BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    text = "", type = "details",
                                    id = TODO(),
                                    clientId = TODO(),
                                    screenId = TODO(),
                                    buttonName = TODO(),
                                    action = TODO(),
                                    isVisible = TODO()
                                )
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        // 💡 UI observes state.showDetails
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
            if (state.showDetails) { // 💡 Observe state.showDetails
                BookingDetails(
                    booking = state.selectedBooking,
                    modifier = Modifier.weight(1f)
                )
            }
        } // END Master/Detail Row
    }
}