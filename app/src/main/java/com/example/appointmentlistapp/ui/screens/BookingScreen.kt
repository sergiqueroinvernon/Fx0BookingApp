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
    // 💡 REMOVED: Deleted the unused 'testResult' state
    // var testResult by remember { mutableStateOf<String?>(null) }
    val buttonConfigs by bookingViewModel.buttonsConfig.collectAsState()

    // Trigger API call once when the screen is first created
    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
    }


    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "BookingScreen Content")
        Spacer(modifier = Modifier.height(16.dp))

        // --- Dynamic Buttons Row ---
        // 💡 FIX 1: Wrap dynamic buttons in a Row and add spacing for a horizontal toolbar look.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (buttonConfigs.isNotEmpty()) {
                // Iterate through all the items
                buttonConfigs.forEach { config ->
                    Button(
                        // 💡 FIX 2: Change the onClick action to a meaningful, but currently
                        // placeholder action. The hardcoded 'showDetails = !showDetails'
                        // logic is moved to a separate toggle button.
                        onClick = {
                            // TODO: Implement button-specific logic based on config.type/action
                            println("API Button '${config.text}' clicked!")
                        }
                    ) {
                        Icon(
                            painter = painterResource(getIconForType(config.type.toString().trim())),
                            // 💡 FIX 3: Use the button label for content description
                            contentDescription = config.text,
                            modifier = Modifier.size(15.dp).padding(end = 4.dp)
                        )
                        // 💡 FIX 4: Display the actual label from the API configuration
                        Text(config.text)
                    }
                }
            } else {
                Text(text = "Loading buttons...") // Show a loading/placeholder message
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Toggle Details Button (Utility Button) ---
        // 💡 FIX 5: Use a separate, simple button for the specific 'showDetails' logic
        Button(onClick = { showDetails = !showDetails }) {
            Text(if (showDetails) "Hide Details Pane" else "Show Details Pane")
        }


        // 💡 REMOVED: Deleted the old redundant 'Show Test Result' button and display block.
        /*
        Button(onClick = { ... }) { ... }
        testResult?.let { ... }
        */

        Spacer(modifier = Modifier.height(8.dp))

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