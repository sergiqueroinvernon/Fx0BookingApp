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
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel
import com.example.appointmentlistapp.ui.components.BookingItem
import com.google.accompanist.flowlayout.FlowRow // WICHTIGER IMPORT
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingSelected
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingCheckedChange

// --- Helper Composable for Button (Correct) ---
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
            painter = painterResource(getIconForType(config.type.toString().trim())),
            contentDescription = config.text,
            modifier = Modifier.size(14.dp).padding(end = 4.dp)
        )
        Text(config.text)
    }
}
// ---

@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()

    // ✅ FIX 1: Sammle alle individuellen State Flows aus der ViewModel
    val bookings by bookingViewModel.bookings.collectAsState()
    val selectedBooking by bookingViewModel.selectedBooking.collectAsState()
    val buttonConfigs by bookingViewModel.buttonConfigs.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()
    val showDetails by bookingViewModel.showDetails.collectAsState() // NEUER FLOW
    // Der alte "state" (z.B. val state by bookingViewModel.uiState.collectAsState()) wurde entfernt.


    // Fetches button configurations AND initial appointments (for testing) on screen launch
    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
        // ✅ FIX 2: Füge den Aufruf zum Laden der Termine hinzu (mit einer Test-ID)
        bookingViewModel.fetchAppointments("DRIVER_TEST_ID")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- HEADER SECTION (Title and Error) ---
        Text(
            text = "BookingScreen Content",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        errorMessage?.let { msg -> // ✅ Verwende den korrekten 'errorMessage' Flow
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

                // --- Dynamic Buttons Row (FlowRow) ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    // ✅ FIX 3: Korrigierte FlowRow Spacing Parameter
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    if (buttonConfigs.isNotEmpty()) { // ✅ Verwende den korrekten 'buttonConfigs' Flow
                        buttonConfigs.forEach { config ->
                            ActionButton(
                                config,
                                bookingViewModel,
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    } else if (isLoading) { // ✅ Verwende den korrekten 'isLoading' Flow
                        Text(text = "Loading buttons...", modifier = Modifier.padding(8.dp))
                    } else {
                        Text(text = "No buttons configured.", modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Utility Toggle Button (Hardcoded Details Toggle) ---
                // ✅ FIX 4: Nutze 'selectedBooking' und 'showDetails' Flows
                if (selectedBooking != null && buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            bookingViewModel.handleEvent(BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    id = 0, clientId = "client123", screenId = "BookingScreen",
                                    buttonName = "DetailsToggle", action = "TOGGLE_DETAILS",
                                    type = "details", isVisible = 1,
                                    text = if (showDetails) "Hide Details" else "Show Details",
                                    IconData = "" // ✅ FIX 5: TODO() entfernt
                                )
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(if (showDetails) "Hide Details Pane" else "Show Details Pane") // ✅ Nutze 'showDetails'
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Master Pane List (LazyColumn) ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        bookings, // ✅ Nutze den korrekten 'bookings' Flow
                        key = { booking -> booking.id ?: booking.toString() } // Sicherer Key
                    ) { booking -> // ✅ Nutze 'booking' als Iterator
                        BookingItem(
                            booking = booking,
                            // ✅ FIX 6: onClick Handler für Auswahl hinzugefügt
                            onClick={
                                bookingViewModel.handleEvent(BookingSelected(booking))
                            },
                            isSelected = booking.id == selectedBooking?.id,
                            // ✅ FIX 7: isSelected Status hinzugefügt
                            isChecked = booking.isChecked ?: false, // Sicherer Null-Check
                            onCheckedChange = {
                                bookingViewModel.handleEvent(
                                    BookingCheckedChange(booking.id)
                                )
                            },
                        )
                    }
                }
            } // END Master Pane Column

            VerticalDivider()

            // Detail Pane
            if (showDetails) { // ✅ Nutze den korrekten 'showDetails' Flow
                BookingDetails(
                    booking = selectedBooking as Booking?, // ✅ Nutze den korrekten 'selectedBooking' Flow
                    modifier = Modifier.weight(1f) // ✅ Gewicht hinzugefügt
                )
            }
        } // END Master/Detail Row
    }
}