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
import com.example.appointmentlistapp.data.Logbook
import com.example.appointmentlistapp.data.PurposeOfTrip
import com.example.appointmentlistapp.data.Vehicle
import com.example.appointmentlistapp.data.StatusOption
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.ui.components.BookingDetails
import com.example.appointmentlistapp.util.getIconForType
import com.example.appointmentlistapp.viewmodels.BookingEvent
import com.example.appointmentlistapp.viewmodels.BookingViewModel
import com.example.appointmentlistapp.ui.components.BookingItem
import com.example.appointmentlistapp.ui.components.LogbookDetailView
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterEvent
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterState
import com.google.accompanist.flowlayout.FlowRow
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingSelected
import com.example.appointmentlistapp.viewmodels.BookingEvent.BookingCheckedChange

// --- Helper Composable for Button ---
@Composable
private fun ActionButton(
    config: ButtonConfig,
    viewModel: BookingViewModel,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (config.type.toString().trim().equals("filter", ignoreCase = true)) {
                onFilterClick()
            } else {
                viewModel.handleEvent(BookingEvent.ButtonClicked(config))
            }
        },
        modifier = modifier,
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

// --- CORRECTED LOGBOOK FILTER MASK (Consolidated LoockBookFilterMask & LogBookFilterScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogBookFilterMask( // Renamed and fixed signature
    purposeOfTrips: List<PurposeOfTrip>,
    statusOptions: List<StatusOption>,
    vehiclesByDriverId: List<Vehicle>,
    filterState: LogBookFilterState, // Using LogBookState
    onEvent: (LogBookFilterEvent) -> Unit, // ADDED MISSING onEvent parameter and used LogBookEvent
    onClose: () -> Unit // Function to close the dialog
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var travelPurposeDropdownExpanded by remember { mutableStateOf(false) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

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
            Text("Filterkriteriena", style = MaterialTheme.typography.headlineSmall)
            Divider()

            // 1. Eintragsnr. (Using LogBook-specific properties and events)
            OutlinedTextField(
                value = filterState.entryNr,
                onValueChange = { onEvent(LogBookFilterEvent.EntryNrChange(it)) },
                label = { Text("Eintragsnr.") },
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
                    value = if (filterState.status.isNotEmpty()) {
                        statusOptions.find { it.status == filterState.status }?.status ?: "Status"
                    } else {
                        "Status"
                    },
                    onValueChange = {},
                    label = { Text("Status") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth() // Use menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = statusDropdownExpanded, // State is now managed by the parent box
                    onDismissRequest = {
                        statusDropdownExpanded = false
                    }, // Keep this to close on outside click
                    modifier = Modifier.exposedDropdownSize() // Match the width of the anchor
                ) {
                    statusOptions.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.status) },
                            onClick = {
                                onEvent(LogBookFilterEvent.StatusChange(status.status)) // Using LogBook event
                                statusDropdownExpanded = false
                            })
                    }
                }
            }

            // 3. Datum Start (Using LogBook-specific properties and events)
            OutlinedTextField(
                value = filterState.dateStart.ifEmpty { "Datum Start - -" }, // Corrected property
                onValueChange = { onEvent(LogBookFilterEvent.DateStartChange(it)) }, // Using LogBook event
                label = { Text("Datum Start") },
                readOnly = true, // Assuming date picking is external
                trailingIcon = {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        Modifier.clickable {}
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Reisezweck (Using LogBook-specific properties and events)
            ExposedDropdownMenuBox(
                expanded = travelPurposeDropdownExpanded,
                onExpandedChange = {
                    travelPurposeDropdownExpanded = !travelPurposeDropdownExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterState.purposeId.ifEmpty { "Reisezweck" },
                    onValueChange = { /* onValueChange must be defined, but can be empty for readOnly */ },
                    label = { Text("Reisezweck") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = travelPurposeDropdownExpanded,
                    onDismissRequest = { travelPurposeDropdownExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    purposeOfTrips.forEach { purpose ->
                        DropdownMenuItem(
                            text = { Text(purpose.purpose) },
                            onClick = {
                                onEvent(LogBookFilterEvent.PurposeIdChange(purpose.id)) // Using LogBook event
                                travelPurposeDropdownExpanded = false
                            })
                    }
                }
            }
            // 5. Fahrzeug (Using LogBook-specific properties and events)
            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterState.vehicleRegistration.ifEmpty { "Fahrzeug" }, // Corrected property
                    onValueChange = {},
                    label = { Text("Fahrzeug") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    vehiclesByDriverId.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text(vehicle.registration ?: "Unbekanntes Fahrzeug") },
                            onClick = {
                                onEvent(
                                    LogBookFilterEvent.VehicleRegistrationChange( // Using LogBook event
                                        vehicle.registration ?: ""
                                    )
                                )
                                vehicleDropdownExpanded = false
                            })
                    }
                }
            }

            // Action Buttons (Aktualisieren / Zurücksetzen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Aktualisieren (Update)
                Button(
                    onClick = { onEvent(LogBookFilterEvent.ApplyFilter); onClose() }, // Using LogBook event
                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                ) {
                    Text("Aktualisieren")
                }

                // Zurücksetzen (Reset)
                OutlinedButton(
                    onClick = { onEvent(LogBookFilterEvent.ResetFilter); onClose() }, // Using LogBook event
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
fun LogBookScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()

    // State flows from ViewModel
    val bookings by bookingViewModel.bookings.collectAsState()
    val selectedBooking by bookingViewModel.selectedBooking.collectAsState()
    val buttonConfigs by bookingViewModel.buttonConfigs.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()
    val showDetails by bookingViewModel.showDetails.collectAsState()
    val purposeOfTrips by bookingViewModel.purposeOfTrips.collectAsState()
    val statusOptions by bookingViewModel.statusOptions.collectAsState() // New state
    val vehiclesBYDriverId by bookingViewModel.vehiclesBYDriverId.collectAsState()

    // Assuming the ViewModel can provide a LogBookFilterState for this screen, or mapping it
    // For now, we will use a derived state based on the existing BookingFilterState
    val currentFilterState = bookingViewModel.filterState.collectAsState().value

    // Placeholder handler for LogBookFilterEvents (since we are currently using BookingViewModel)
    val handleLogBookFilterEvent: (LogBookFilterEvent) -> Unit = { event ->
        // In a real application, you would map this LogBook event to the specific ViewModel logic
        println("LogBook Filter Event Handled: $event")
    }

    var showFilterMask by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        // These calls should ideally be adapted for Logbook data
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "LogBookScreen")
        bookingViewModel.fetchAppointments("DRIVER_TEST_ID") // Placeholder data fetch
        bookingViewModel.fetchPurposeOfTrips()
        bookingViewModel.fetchStatusOptions()
        bookingViewModel.fetchVehiclesByDriver("F7F5C431-E776-48B4-B9BC-9ABA528E6F23")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- FILTER DIALOG (Pop-up Window) ---
        if (showFilterMask) {
            Dialog(onDismissRequest = { showFilterMask = false }) {
                LogBookFilterMask( // Using the correct LogBookFilterMask
                    purposeOfTrips = purposeOfTrips,
                    statusOptions = statusOptions,
                    vehiclesByDriverId = vehiclesBYDriverId,
                    filterState = LogBookFilterState( // Creating a mock LogBookState from current state
                        entryNr = currentFilterState.entryNr,
                        status = currentFilterState.status,
                        dateStart = currentFilterState.handOverDate, // Mapping property
                        purposeId = currentFilterState.purposeId,
                        vehicleRegistration = currentFilterState.vehicle
                    ),
                    onEvent = handleLogBookFilterEvent,
                    onClose = { showFilterMask = false }
                )
            }
        }
        // -------------------------------------

        // --- HEADER SECTION (Title and Error) ---
        Text(
            text = "Bearbeitung meiner Fahrtenbuch-Einträge", // Corrected title
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
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

                // FILTERKRITERIEN BUTTON to open the Dialog


                // --- Dynamic Buttons Rowd (FlowRow) ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    if (buttonConfigs.isNotEmpty()) {
                        buttonConfigs.forEach { config ->
                            ActionButton(
                                config,
                                bookingViewModel,
                                onFilterClick = { showFilterMask = true },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    } else if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.padding(8.dp).size(24.dp))
                    } else {
                        Text(
                            text = "No buttons configured.",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                                Text("Keine Einträge gefunden.")
                            }
                        }
                    } else {
                        items(
                            bookings,
                            key = { booking ->
                                booking.bookingId ?: booking.toString()
                            }) { booking ->
                            BookingItem(
                                booking = booking,
                                onClick = {
                                    bookingViewModel.handleEvent(
                                        BookingSelected(
                                            booking.bookingId
                                        )
                                    )
                                },
                                isSelected = booking.bookingId == selectedBooking?.bookingId,
                                isChecked = booking.isChecked ?: false,
                                onCheckedChange = {
                                    bookingViewModel.handleEvent(
                                        BookingCheckedChange(booking.bookingId)
                                    )
                                },
                            )
                        }
                    }
                } // END Master Pane List
            } // END Master Pane Column

            VerticalDivider()
/*
            // Detail Pane
            if (showDetails) {
                LogbookDetailView( // Should ideally be LogbookDetailView
                    logBook = selectedBooking as Logbook?,
                    modifier = Modifier.weight(1f)
                )
            }
            */

        } // END Master/Detail Row
    }
}