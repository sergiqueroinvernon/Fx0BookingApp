package com.example.appointmentlistapp.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.appointmentlistapp.data.* // Import all data classes
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.ui.components.filters.BookingFilterEvent
import com.example.appointmentlistapp.ui.components.filters.BookingFilterState
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterEvent
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterState
import com.example.appointmentlistapp.viewmodels.BookingEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

// Using the provided Logbook data class properties for clarity
data class LogBookUiState(
    val entries: List<Logbook> = emptyList(),
    val selectedEntry: Logbook? = null,
    val checkedEntryIds: Set<Long> = emptySet(),
    val isDetailPlainVisible: Boolean = false,
    val isFilterPlaneVisible: Boolean = false,
    val isInEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LogBookEvent {
    data class ButtonClicked(val config: ButtonConfig) : LogBookEvent()
    data class LogbookSelected(val logBook: String) : LogBookEvent()
    data class LogbookCheckedChange(val logbookId: String) : LogBookEvent()
}

@RequiresApi(Build.VERSION_CODES.O)
class LogBookViewModel : ViewModel() {

    //Data
    private fun getStartOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDay(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // Parses the date string from your API (assuming "dd.MM.yyyy")
    private fun parseDateString(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        return try {
            // Define the format that matches the API string
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN)

            // Parse the string, but ignore the fractional seconds by splitting the string
            formatter.parse(dateStr.substringBefore("."))?.time

        } catch (e: Exception) {
            Log.e("LookingViewModel", "Failed to parse date string: $dateStr", e)
            null
        }
    }

    private val _filterState = MutableStateFlow(
        LogBookFilterState(
            entryNr = "",
            status = "",
            dateStart = "",
            purpose = "",
            vehicleRegistration = ""
        )
    )

    private val _tempFilterState = MutableStateFlow(
        LogBookFilterState(
            entryNr = "",
            status = "",
            dateStart = "",
            purpose = "",
    )
    )

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId.asStateFlow()


    val filterState: StateFlow<LogBookFilterState> = _tempFilterState.asStateFlow()
    private val _activeFilterState = MutableStateFlow<LogBookFilterState?>(LogBookFilterState(
        entryNr = "",
        status = "",
        dateStart = "",
        purpose = "",
        vehicleRegistration = ""
    ))

    fun handleFilterEvent(event: LogBookFilterEvent) {
        when (event) {

            // These all update the _tempFilterState
            is LogBookFilterEvent.EntryNrChange-> _tempFilterState.update { it.copy(entryNr = event.entryNo) }
            is LogBookFilterEvent.StatusChange -> _tempFilterState.update { it.copy(status = event.status) }
            is LogBookFilterEvent.DateStartChange -> _tempFilterState.update { it.copy(dateStart = event.date) }
            is LogBookFilterEvent.PurposeIdChange -> _tempFilterState.update { it.copy(purpose = event.purposeId.toString()) }
            is LogBookFilterEvent.VehicleRegistrationChange -> _tempFilterState.update { it.copy(vehicleRegistration = event.registrationName) }

            LogBookFilterEvent.ApplyFilter -> {
                _activeFilterState.value = _tempFilterState.value
            }

            LogBookFilterEvent.ResetFilter -> {
                _tempFilterState.value = LogBookFilterState(
                    entryNr = "",
                    status = "",
                    dateStart = "",
                    purpose = "",
                    vehicleRegistration = ""
                )
                _activeFilterState.value = LogBookFilterState(
                    entryNr = "",
                    status = "",
                    dateStart = "",
                    purpose = "",
                    vehicleRegistration = ""
                )
                Log.d("ViewModel", "Filter reset")
            }

            LogBookFilterEvent.ApplyFilter -> {
                // THIS IS THE FIX: Copy the temp state to the active state.
                // This will trigger the .combine() on the 'bookings' flow.
                _activeFilterState.value = _tempFilterState.value
                Log.d("ViewModel", "Filter *APPLIED*.")
            }

            LogBookFilterEvent.ResetFilter -> {
                // Reset BOTH states
                _tempFilterState.value = LogBookFilterState(
                    entryNr = "",
                    status = "",
                    dateStart = "",
                    purpose = "",
                )
                _activeFilterState.value = LogBookFilterState(
                    entryNr = "",
                    status = "",
                    purpose = "",
                    dateStart = "",
                )
                Log.d("ViewModel", "Filter reset.")
            }
        }
    }

    private val _purposeOfTrips = MutableStateFlow<List<PurposeOfTrip>>(emptyList())
    val purposeOfTrips: StateFlow<List<PurposeOfTrip>> = _purposeOfTrips.asStateFlow();

    private val _statusOptions = MutableStateFlow<List<StatusOption>>(emptyList())
    val statusOptions: StateFlow<List<StatusOption>> = _statusOptions.asStateFlow();

    private val _vehiclesBYDriverId = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehiclesBYDriverId: StateFlow<List<Vehicle>> = _vehiclesBYDriverId.asStateFlow();

    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val allAppointments: StateFlow<List<Appointment>> = _allAppointments.asStateFlow()

    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    val buttonConfigs: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()

    private val _showDetails = MutableStateFlow(true)
    val showDetails: StateFlow<Boolean> = _showDetails.asStateFlow()

    fun fetchLogBookEntries(
        driverId: String? = _scannedDriverId.value,
        useCachedData: Boolean = false
    ) {

    }



    private val _selectedLogBook = MutableStateFlow<Logbook?>(null)
    val selectedLogBook: StateFlow<Logbook?> = _selectedLogBook.asStateFlow()

    // 1. Consolidated state management into a single MutableStateFlow
    private val _uiState = MutableStateFlow(
        LogBookUiState(
            entries = createMockLogbookEntries() // Initialize with mock data
        )
    )
    // Public immutable state that the UI can observe
    val uiState: StateFlow<LogBookUiState> = _uiState.asStateFlow()



    // 2. Initializer block now correctly sets the selected entry using the unified state
    init {
        _uiState.update { currentState ->
            currentState.copy(
                selectedEntry = currentState.entries.firstOrNull(),
                isDetailPlainVisible = true // Show details for the first entry by default
            )
        }
    }






    fun toggleLogBookChecked(logbookId: String) {
        // 1. Actualitza el valor del Flow mestre, creant una nova llista
        _allAppointments.value = _allAppointments.value.map { appointment ->
            if (appointment.id == logbookId) {
                appointment.copy(isChecked = !appointment.isChecked)
            } else {
                appointment            }
        }

        // La línia anterior dispara automàticament el filtre 'combine'.
    }


    private fun handleButtonClicked(config: ButtonConfig) {
        when(config.type.lowercase().trim()){
            "details" -> _showDetails.value = !_showDetails.value
            "add" -> Log.d("ViewModel", "Action: Navigate to ADD screen.")
            "edit" -> Log.d(
                "ViewModel",
                "Action: Navigate to EDIT screen for booking ID: ${_selectedLogBook.value?.entryNr}"
            )
            else -> Log.w("ViewModel", "Unknown button action type: ${config.type}")
        }
    }

    fun selectLogBook(logBookId: String) {
        val selectedAppointment = _allAppointments.value.find { it.id == logBookId }
        if (selectedAppointment != null) {
            _selectedLogBook.value
        }
    }


    fun handleEvent(event: LogBookEvent) {
        when (event) {
            is LogBookEvent.ButtonClicked -> handleButtonClicked(event.config)
            is LogBookEvent.LogbookCheckedChange -> selectLogBook(event.logbookId)
            is LogBookEvent.LogbookSelected -> toggleLogBookChecked(event.logBook)
        }
    }


    /**
     * Updates the selected entry in the UI state.
     */
    fun selectEntry(entryId: Long) {
        _uiState.update { currentState ->
            val newSelectedEntry = currentState.entries.find { it.entryNr == entryId }
            currentState.copy(
                selectedEntry = newSelectedEntry,
                // Ensure detail view is visible when an entry is selected
                isDetailPlainVisible = newSelectedEntry != null
            )
        }
    }

    /**
     * Toggles the checked status for a specific entry.
     */
    fun toggleEntryChecked(entryId: Long) {
        _uiState.update { currentState ->
            val isChecked = currentState.checkedEntryIds.contains(entryId)
            val newCheckedEntryIds = if (isChecked) {
                currentState.checkedEntryIds - entryId
            } else {
                currentState.checkedEntryIds + entryId
            }

            currentState.copy(
                checkedEntryIds = newCheckedEntryIds,
                // Also update the isChecked flag directly in the Logbook model if desired
                entries = currentState.entries.map { entry ->
                    if (entry.entryNr == entryId) {
                        entry.copy(isChecked = !isChecked)
                    } else {
                        entry
                    }
                }
            )
        }
    }

    // You can add functions to toggle visibility of panes here:
    fun toggleDetailPaneVisibility() {
        _uiState.update { it.copy(isDetailPlainVisible = !it.isDetailPlainVisible) }
    }

    // You can also add actions like `confirmEntries()` which would utilize `checkedEntryIds`

    // --- MOCK DATA CREATION (Uses the corrected data model) ---

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createMockLogbookEntries(): List<Logbook> {
        return listOf(
            Logbook(
                entryNr = 1000351477, // Corrected property name
                status = LogbookStatus.CONFIRMED.name, // Use name property
                startTime = LocalDateTime.of(2025, 7, 21, 13, 33),
                endTime = LocalDateTime.of(2025, 7, 21, 13, 45),
                vehicle = Vehicle(
                    registration = "D-WG 466E",
                    id = "V001",
                    poolId = TODO(),
                    // poolId property missing in new Logbook definition, removing from Vehicle mock
                ),
                internalNumber = "INT477", // Added missing property
                startOdometer = 7974.0,
                endOdometer = 7984.0,
                purposeOfTrip = "Kundenbesuch", // Corrected property name
                distance = 10.0,
                startLocation = "Firmenzentrale, Düsseldorf",
                endLocation = "Kundenadresse, Düsseldorf", // Added missing property
                isReactionTimeDriver = false,
                isResponseTimeTrip = false,
                justification = null,
                tripLegs = emptyList(),
                approval = Approval(
                    approvedOn = LocalDate.of(2025, 7, 22),
                    approvedBy = "Max Mustermann",
                    notes = "Fahrt genehmigt."
                ),
                cancellation = null,
                notes = "Keine besonderen Vorkommnisse."
            ),
            Logbook(
                entryNr = 1000351454,
                status = LogbookStatus.NOT_CONFIRMED.name,
                startTime = LocalDateTime.of(2025, 7, 21, 12, 48),
                endTime = LocalDateTime.of(2025, 7, 21, 13, 15),
                vehicle = Vehicle(
                    registration = "D-EM 719E",
                    id = "V002",
                    poolId = TODO(),
                ),
                internalNumber = "INT454",
                startOdometer = 7964.0,
                endOdometer = 7974.0,
                purposeOfTrip = "Baustelle",
                distance = 10.0,
                startLocation = "Lager, Köln",
                endLocation = "Baustelle A, Köln",
                isReactionTimeDriver = true,
                isResponseTimeTrip = true,
                justification = "Dringender Materialtransport",
                tripLegs = listOf(
                    TripLeg(
                        startTime = LocalDateTime.of(2025, 7, 21, 12, 48),
                        startLocation = "Lager, Köln",
                        endTime = LocalDateTime.of(2025, 7, 21, 13, 15),
                        endLocation = "Baustelle A, Köln"
                    )
                ),
                approval = null,
                cancellation = null,
                notes = null
            )
        )
    }
}

