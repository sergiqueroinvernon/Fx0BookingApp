package com.example.appointmentlistapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import com.example.appointmentlistapp.data.PurposeOfTrip
import com.example.appointmentlistapp.data.Vehicle
import com.example.appointmentlistapp.data.StatusOption
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
    modifier: Modifier = Modifier,
    onFilterClick: () -> Unit

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

// --- NEW COMPOSABLE: The Filter Mask (Content for the Dialog) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFilterMask(
    purposeOfTrips: List<PurposeOfTrip>,
    statusOptions: List<StatusOption>,
    datePickerStateStart: DatePickerState,
    datePickerStateEnd: DatePickerState,
    vehiclesByDriverId: List<Vehicle>,
    filterState: BookingFilterState,
    onEvent: (BookingFilterEvent) -> Unit,
    onClose: () -> Unit // Function to close the dialog
) {
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var travelPurposeDropdownExpanded by remember { mutableStateOf(false) }
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }


    var showDatePickerStart by remember { mutableStateOf(false) }

    var showDatePickerEnd by remember { mutableStateOf(false) }




    var dateFormatter = remember {java.text.SimpleDateFormat("dd.Mm.yyyy", java.util.Locale.GERMAN)}


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
                value = filterState.entryNr,
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
                            text = {
                                Text(
                                    status.status
                                )
                            },
                            onClick = {
                                onEvent(BookingFilterEvent.StatusChange(status.status))
                                statusDropdownExpanded = false
                            })
                    }
                }
            }

            // 3. Übergabedatum (Date Range Placeholder)
            Text("Übergabe", style = MaterialTheme.typography.bodyLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 📝 Field for "Von" (Start Date)
                OutlinedTextField(
                    // Display the current start date from the filter state, or empty string
                    value = filterState.handOverDateStart?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = { }, // Read-only
                    label = { Text("Von") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePickerStart = true }

                    // 👈 Opens the 'Von' Date Picker Dialog
                )

                // 📝 Field for "Bis" (End Date)
                OutlinedTextField(
                    // Display the current end date from the filter state, or empty string
                    value = filterState.handOverDataEnd?.let { dateFormatter.format(it) } ?: "",
                    onValueChange = { }, // Read-only
                    label = { Text("Bis") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {showDatePickerEnd = true}
                        // 👈 Opens the 'Bis' Date Picker Dialog
                )
            }

            // 4. DatePickerDialog for "Von" (Start Date)
            if (showDatePickerStart) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerStart = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePickerStart = false
                            datePickerStateStart.selectedDateMillis?.let {
                                onEvent(BookingFilterEvent.HandOverDataStartChange(it))
                            }
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerStart = false }) {
                            Text("Abbrechen")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerStateStart,
                        headline = { Text("Startdatum auswählen", modifier = Modifier.padding(16.dp)) }
                    )
                }
            }

            // 5. DatePickerDialog for "Bis" (End Date)
            if (showDatePickerEnd) {
                DatePickerDialog(
                    onDismissRequest = { showDatePickerEnd = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDatePickerEnd = false
                            // Using datePickerStateEnd here
                            datePickerStateEnd.selectedDateMillis?.let {
                                onEvent(BookingFilterEvent.HandOverDataEndChange(it))
                            }
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePickerEnd = false }) {
                            Text("Abbrechen")
                        }
                    }
                ) {
                    DatePicker(
                        state = datePickerStateEnd,
                        headline = { Text("Enddatum auswählen", modifier = Modifier.padding(16.dp)) }
                    )
                }
            }

            ExposedDropdownMenuBox(
                expanded = travelPurposeDropdownExpanded,
                onExpandedChange = {
                    travelPurposeDropdownExpanded = !travelPurposeDropdownExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterState.travelPurposeChange.ifEmpty { "Reisezweck" },
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
                                onEvent(BookingFilterEvent.TravelPurposeChange(purpose = purpose.purpose))
                                travelPurposeDropdownExpanded = false
                            })
                    }
                }
            }

            // 5. Fahrzeug (Dropdown Placeholder)
            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = filterState.registrationName.ifEmpty { "Fahrzeug" },
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
                                    BookingFilterEvent.RegistrationName(
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
        } // END Column
    } // END Surface
}


// ----------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen() {
    val bookingViewModel = viewModel<BookingViewModel>()

    // State flows from ViewModel
    val appointments by bookingViewModel.appointmentsUI.collectAsState()
    val bookings by bookingViewModel.bookings.collectAsState()
    val selectedBooking by bookingViewModel.selectedBooking.collectAsState()
    val buttonConfigs by bookingViewModel.buttonConfigs.collectAsState()
    val isLoading by bookingViewModel.isLoading.collectAsState()
    val errorMessage by bookingViewModel.errorMessage.collectAsState()
    val showDetails by bookingViewModel.showDetails.collectAsState()
    val purposeOfTrips by bookingViewModel.purposeOfTrips.collectAsState()
    val statusOptions by bookingViewModel.statusOptions.collectAsState() // New state
    val vehiclesBYDriverId by bookingViewModel.vehiclesBYDriverId.collectAsState()

    // 🆕 NEW: State to toggle the visibility of the filter mask Dialog
    val filterState by bookingViewModel.filterState.collectAsState()

    var showFilterMask by remember { mutableStateOf(false) }

    // Hoist the DatePickerState to BookingScreen to preserve state across recompositions
    val datePickerStateStart = rememberDatePickerState(
        initialSelectedDateMillis = filterState.handOverDateStart,
    )
    val datePickerStateEnd = rememberDatePickerState(
        initialSelectedDateMillis = filterState.handOverDataEnd,
    )

    // When the filter state is reset, we need to update the date pickers' displayed dates
    LaunchedEffect(filterState.handOverDateStart) {
        datePickerStateStart.selectedDateMillis = filterState.handOverDateStart
    }
    LaunchedEffect(filterState.handOverDataEnd) {
        datePickerStateEnd.selectedDateMillis = filterState.handOverDataEnd
    }

    LaunchedEffect(Unit) {
        bookingViewModel.fetchButtonsForClientAndScreen("client123", "BookingScreen")
        bookingViewModel.fetchAppointments("DRIVER_TEST_ID")
        bookingViewModel.fetchPurposeOfTrips()
        bookingViewModel.fetchStatusOptions()
        bookingViewModel.fetchVehiclesByDriver("FD104CC0-4756-4D24-8BDF-FF06CF716E22")
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // --- FILTER DIALOG (Pop-up Window) ---
        if (showFilterMask) {
            Dialog(onDismissRequest = { showFilterMask = false }) {
                BookingFilterMask(
                    purposeOfTrips = purposeOfTrips,
                    statusOptions = statusOptions, // Pass the dynamic list
                    datePickerStateStart = datePickerStateStart,
                    datePickerStateEnd = datePickerStateEnd,
                    vehiclesByDriverId = vehiclesBYDriverId,
                    filterState = filterState,
                    onEvent = bookingViewModel::handleFilterEvent,
                    onClose = { showFilterMask = false }
                )
            }
        }
        // -------------------------------------

        // --- HEADER SECTION (Title and Error) ---
        Text(
            text = "Bearbeitung meiner Buchungen von Poolfahrzeugen",
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

                // --- Dynamic Buttons Rowd (FlowRow) ---
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    if (buttonConfigs.isNotEmpty()) {
                        buttonConfigs.forEach { config ->
                            ActionButton(
                                config = config,
                                viewModel = bookingViewModel,
                                modifier = Modifier.width(160.dp),
                                onFilterClick = { showFilterMask = true }
                            )
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
                        items(bookings, key = { booking -> booking.bookingId ?: booking.toString() }) { booking ->
                            BookingItem(
                                booking = booking,
                                onClick = { bookingViewModel.handleEvent(BookingSelected(booking.bookingId)) },
                                isSelected = booking.bookingId == selectedBooking?.bookingId,
                                isChecked = booking.isChecked ?: false,
                                onCheckedChange = { bookingViewModel.handleEvent(BookingCheckedChange(booking.bookingId)) },
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