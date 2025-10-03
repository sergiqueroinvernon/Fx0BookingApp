package com.example.appointmentlistapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.window.Dialog // Import for the pop-up window

import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel
import com.example.appointmentlistapp.ui.components.BookingItem
import com.example.appointmentlistapp.ui.components.filters.BookingFilterEvent
import com.example.appointmentlistapp.ui.components.filters.BookingFilterState
import com.google.accompanist.flowlayout.FlowRow
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingSelected
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingCheckedChange

// --- Filter State Placeholders (Should exist in your ViewModel package) ---
// ---

// --- Helper Composable for Button ---
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

// --- NEW COMPOSABLE: The Filter Mask (Content for the Dialog) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFilterMask(
    filterState: BookingFilterState,
    onEvent: (BookingFilterEvent) -> Unit,
    onClose: () -> Unit // Function to close the dialog
) {
    val statusOptions = listOf("Bereit zur Übergabe", "Übergeben", "Storniert")
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Use a Surface inside the Dialog for style and elevation
    Surface(
        modifier = Modifier
            .width(400.dp) // Define a width suitable for a standard pop-up
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Filterkriterien", style = MaterialTheme.typography.headlineSmall)
            Divider()

            // 1. Vorgangsnr.
            OutlinedTextField(
                value = filterState.bookingNo,
                onValueChange = { onEvent(BookingFilterEvent.BookingNoChange(it)) },
                label = { Text("Vorgangsnr.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 2. Status (Dropdown Placeholder)
            ExposedDropdownMenuBox(
                expanded = statusDropdownExpanded,
                onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterState.status.ifEmpty { "Status" },
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth() // Use menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded, // State is now managed by the parent box
                    onDismissRequest = { statusDropdownExpanded = false }, // Keep this to close on outside click
                    modifier = Modifier.exposedDropdownSize() // Match the width of the anchor
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                onEvent(BookingFilterEvent.StatusChange(status))
                                statusDropdownExpanded = false
                            })
                    }
                }
            }

            // 3. Übergabedatum (Date Range Placeholder)
            OutlinedTextField(
                value = filterState.handOverDate.ifEmpty { "Übergabedatum - -" },
                onValueChange = {},
                label = { Text("Übergabedatum") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, Modifier.clickable {}) },
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Reisezweck (Dropdown Placeholder)
            OutlinedTextField(
                value = filterState.travelPurpose.ifEmpty { "Reisezweck" },
                onValueChange = {},
                label = { Text("Reisezweck") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, Modifier.clickable {}) },
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Fahrzeug (Dropdown Placeholder)
            OutlinedTextField(
                value = filterState.vehicle.ifEmpty { "Fahrzeug" },
                onValueChange = {},
                label = { Text("Fahrzeug") },
                readOnly = true,
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, Modifier.clickable {}) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Action Buttons (Aktualisieren / Zurücksetzen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Aktualisieren (Update)
                Button(
                    onClick = { onEvent(BookingFilterEvent.ApplyFilter); onClose() },
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                    // Using default primary color
                ) {
                    Text("Aktualisieren")
                }

                // Zurücksetzen (Reset)
                OutlinedButton(
                    onClick = { onEvent(BookingFilterEvent.ResetFilter); onClose() },
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                ) {
                    Text("Zurücksetzen")
                }
            }
        }
    }
}
// ----------------------------------------------------------------------


@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()

    // State flows from ViewModel
    val bookings by bookingViewModel.bookings.collectAsState()
    val selectedBooking by bookingViewModel.selectedBooking.collectAsState()
    val buttonConfigs by bookingViewModel.buttonConfigs.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()
    val showDetails by bookingViewModel.showDetails.collectAsState()



    // 🆕 NEW: State to toggle the visibility of the filter mask Dialog
    // 🆕 NEW: Placeholder for filter state (replace with actual ViewModel state)
    val filterState by bookingViewModel.filterState.collectAsState()

    var showFilterMask by remember { mutableStateOf(false) }



    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
        bookingViewModel.fetchAppointments("DRIVER_TEST_ID")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- FILTER DIALOG (Pop-up Window) ---
        if (showFilterMask) {
            Dialog(onDismissRequest = { showFilterMask = false }) {
                BookingFilterMask(
                    filterState = filterState, // Replace with bookingViewModel.filterState.collectAsState().value
                    onEvent =  { event -> bookingViewModel.handleFilterEvent(event)}, /* Handle filter event in ViewModel */
                    onClose = { showFilterMask = false }
                )
            }
        }
        // -------------------------------------

        // --- HEADER SECTION (Title and Error) ---
        Text(
            text = "BookingScreen Content",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        errorMessage?.let { msg ->
            Text(
                text = "ERROR: $msg",
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // --- MASTER/DETAIL LAYOUT START ---
        Row(Modifier.fillMaxSize()) {

            // Master Pane Column (Filter Button + Buttons + List)
            Column(modifier = Modifier.weight(2f)) {

                // 🆕 NEW: FILTERKRITERIEN BUTTON to open the Dialog
                Button(
                    onClick = { showFilterMask = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Filterkriterien")
                }

                // --- Dynamic Buttons Row (FlowRow) ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    if (buttonConfigs.isNotEmpty()) {
                        buttonConfigs.forEach { config ->
                            ActionButton(config, bookingViewModel, modifier = Modifier.width(160.dp))
                        }
                    } else if (isLoading) {
                        Text(text = "Loading buttons...", modifier = Modifier.padding(8.dp))
                    } else {
                        Text(text = "No buttons configured.", modifier = Modifier.padding(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Utility Toggle Button ---
                if (selectedBooking != null && buttonConfigs.none { it.type.toString().trim().equals("details", ignoreCase = true) }) {
                    Button(
                        onClick = {
                            bookingViewModel.handleEvent(BookingEvent.ButtonClicked(
                                config = ButtonConfig(
                                    id = 0, clientId = "client123", screenId = "BookingScreen",
                                    buttonName = "DetailsToggle", action = "TOGGLE_DETAILS",
                                    type = "details", isVisible = 1,
                                    text = if (showDetails) "Hide Details" else "Show Details", IconData = ""
                                )
                            ))
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(if (showDetails) "Hide Details Pane" else "Show Details Pane")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Master Pane List (LazyColumn) ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bookings.isEmpty() && !isLoading) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Keine Termine gefunden.")
                            }
                        }
                    } else {
                        items(bookings, key = { booking -> booking.id ?: booking.toString() }) { booking ->
                            BookingItem(
                                booking = booking,
                                onClick = { bookingViewModel.handleEvent(BookingSelected(booking)) },
                                isSelected = booking.id == selectedBooking?.bookingId,
                                isChecked = booking.isChecked ?: false,
                                onCheckedChange = { bookingViewModel.handleEvent(BookingCheckedChange(booking.id)) },
                            )
                        }
                    }
                } // END Master Pane List
            } // END Master Pane Column

            VerticalDivider()

            // Detail Pane
            if (showDetails) {
                BookingDetails(
                    booking = selectedBooking as Booking?,
                    modifier = Modifier.weight(1f)
                )
            }
        } // END Master/Detail Row
    }
}