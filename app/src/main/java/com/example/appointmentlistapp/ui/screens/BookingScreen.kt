package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.data.Booking // Assumed data model
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel


// --- Helper Composable for Button (Corrected size) ---
@Composable
private fun ActionButton(config: ButtonConfig, viewModel: BookingViewModel) {
    Button(
        onClick = { viewModel.handleEvent(BookingEvent.ButtonClicked(config)) }
    ) {
        Icon(
            painter = painterResource(getIconForType(config.type.toString().trim())),
            contentDescription = config.text,
            modifier = Modifier.size(24.dp).padding(end = 4.dp)
        )
        Text(config.text)
    }
}
// ---

// --- NEW COMPOSABLE: MASTER DETAIL LIST ---
// This component now displays the detailed row layout (Status, Vorgangsnummer, Kunde)
@Composable
fun MasterBookingList(
    bookings: List<Booking>,
    onBookingSelected: (Booking) -> Unit,
    onBookingCheckedChange: (String) -> Unit, // Assuming ID is a String
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            bookings,
            key = { it.bookingId } // Use the unique ID for stable list keys
        ) { booking ->
            // Replaced generic structure with detailed content layout inside a clickable row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onBookingSelected(booking) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox for selection (Assumed: Booking model has isChecked property)
                    Checkbox(
                        checked = booking.isChecked,
                        onCheckedChange = { onBookingCheckedChange(booking.bookingId) },
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Detailed Columns in the row
                    Column(Modifier.weight(0.5f)) {
                        Text("Status:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(booking.status, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                    VerticalDivider(Modifier.height(40.dp).padding(horizontal = 4.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Vorgangsnummer:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(booking.bookingId, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    }
                    VerticalDivider(Modifier.height(40.dp).padding(horizontal = 4.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Kunde:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text(booking.driver, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Divider() // Separator line for better visual distinction
            }
        }
    }
}
// ---

@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()
    // Consolidated UI state
    val state by bookingViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- HEADER SECTION (Title and Error) ---

        Text(
            text = "BookingScreen Content",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.headlineMedium
        )

        state.errorMessage?.let { msg ->
            Text(
                text = "ERROR: $msg",
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

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

                // --- Utility Toggle Button ---
                if (state.buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            bookingViewModel.handleEvent(BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    id = 0, clientId = "client123", screenId = "BookingScreen", buttonName = "DetailsToggle",
                                    action = "TOGGLE_DETAILS", type = "details", isVisible = 1, text = "Details",
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

                // Master Pane (List) - Using the new detailed list component
                MasterBookingList(
                    bookings = state.bookings as List<Booking>,
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