package com.example.appointmentlistapp.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.* // Import all data classes
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterEvent
import com.example.appointmentlistapp.ui.components.filters.LogBookFilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
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
    data class LogbookSelected(val logBook: Logbook) : LogBookEvent()
    data class LogbookCheckedChange(val logbookId: Long) : LogBookEvent()
}


private val _selectedLogBook = MutableStateFlow<Logbook?>(null)
val selectedLogBook: StateFlow<Logbook?> = _selectedLogBook.asStateFlow()

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
    private fun parseDateString(dateStr: LocalDateTime?): Long? {
        if (dateStr == null) return null
        return try {
            // Define the format that matches the API string
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.GERMAN)

            // Parse the string, but ignore the fractional seconds by splitting the string
            formatter.parse(dateStr.toString().substringBefore("."))?.time

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
    private val _activeFilterState = MutableStateFlow(LogBookFilterState(
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
        }
    }


    private fun applyFilterToLogBooks(
        logBooks: List<Logbook>,
        filter: LogBookFilterState
    ): List<Logbook> {

        // 1. Updated "isDefault" check
        val isFilterEmpty = 
                filter.entryNr.isBlank() &&
                filter.status.isBlank() &&
                filter.dateStart.isBlank() &&
                filter.vehicleRegistration.isBlank() &&
                filter.purpose.isBlank()


        if (isFilterEmpty) {
            return logBooks
        }

        return logBooks.filter { logBook ->

            // 1. Vorgangsnr.
            // The entryNr in Logbook is a Long, but the filter is a String.
            // We need to convert the Logbook entryNr to a String for the comparison.
            val matchesEntryNr = filter.entryNr.isBlank() || logBook.entryNr.toString()
                .contains(filter.entryNr, ignoreCase = true)

            // 2. Status
            val matchesStatus = filter.status.isBlank() ||
                    logBook.status.orEmpty()
                        .contains(filter.status, ignoreCase = true)

            // 3. Vehicle Registration
            val matchesVehicle = filter.vehicleRegistration.isBlank() ||
                    logBook.vehicle?.registration.orEmpty()
                        .contains(filter.vehicleRegistration, ignoreCase = true)

            // 4. Purpose - Assuming purpose filter holds the purpose ID as a string
            val matchesPurpose = filter.purpose.isBlank() ||
                    logBook.purposeOfTrip.toString() == filter.purpose

            // 5. Date
            val appointmentStartTime = parseDateString(logBook.startTime)
            val matchesStartDate = filter.dateStart.isBlank() || (
                    appointmentStartTime != null &&
                            filter.dateStart.toLongOrNull()?.let { filterMillis ->
                                appointmentStartTime >= getStartOfDay(filterMillis) && appointmentStartTime <= getEndOfDay(filterMillis)
                            } ?: true)

            // Combine all conditions
            matchesEntryNr && matchesStatus && matchesVehicle && matchesPurpose && matchesStartDate
        }
    }



    private val _purposeOfTrips = MutableStateFlow<List<PurposeOfTrip>>(emptyList())
    val purposeOfTrips: StateFlow<List<PurposeOfTrip>> = _purposeOfTrips.asStateFlow();

    private val _statusOptions = MutableStateFlow<List<StatusOption>>(emptyList())
    val statusOptions: StateFlow<List<StatusOption>> = _statusOptions.asStateFlow();

    private val _logBooks = MutableStateFlow<List<Logbook>>(emptyList())

    val logBooks: StateFlow<List<Logbook>> =
        combine(_logBooks, _activeFilterState) { books, filter ->
            applyFilterToLogBooks(books, filter)
        }.stateIn(
            // Using combine and stateIn to create a derived state
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
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
        // If we want to use cached data and it's already there, don't fetch again
        if (useCachedData && _logBooks.value.isNotEmpty()) {
            Log.d("LogBookViewModel", "Using cached logbook data.")
            return
        }

        // A driver ID is required to fetch logbooks
        if (driverId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Fahrer-ID nicht gefunden. Bitte erneut scannen."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch data from the API
                val fetchedLogBooks = RetrofitInstance.logBookApi.getLogbooksByDriverId(driverId)
                _logBooks.value = fetchedLogBooks // Update the raw data flow

                // Update the main UI state
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        entries = fetchedLogBooks,
                        // Automatically select the first entry if the list is not empty
                        selectedEntry = fetchedLogBooks.firstOrNull()
                    )
                }
                Log.d("LogBookViewModel", "Successfully fetched ${fetchedLogBooks.size} logbook entries.")
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler beim Laden der Fahrtenbuch-Einträge."
                    is HttpException -> "API-Fehler: HTTP ${e.code()}"
                    else -> "Unerwarteter Fehler: ${e.message}"
                }
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                Log.e("LogBookViewModel", "Error fetching logbook entries", e)
            }
        }
    }
    val isLoading = MutableStateFlow(false)


    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _selectedLogBook = MutableStateFlow<Logbook?>(null)
    val selectedLogBook: StateFlow<Logbook?> = _selectedLogBook.asStateFlow()

    // 1. Consolidated state management into a single MutableStateFlow
    private val _uiState = MutableStateFlow(
        LogBookUiState(
            entries = emptyList() // Initialize with an empty list
        )
    )
    // Public immutable state that the UI can observe
    val uiState: StateFlow<LogBookUiState> = _uiState.asStateFlow()



    // 2. Initializer block now correctly sets the selected entry using the unified state
    init {
        // We fetch data on demand, so the init block is a good place for setup,
        // but not for initial data fetching unless it's always required at startup.
    }


    fun toggleLogBookChecked(logbookId: Logbook) {
        // 1. Actualitza el valor del Flow mestre, creant una nova llista
        _logBooks.value = _logBooks.value.map { logBook ->
            if (logBook.entryNr == logbookId.entryNr) {
                logBook.copy(isChecked = !logBook.isChecked)
            } else {
                logBook
            }
        }
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

    fun selectLogBook(logBookId: Long) {
        val selectedLogbook = _logBooks.value.find { it.entryNr == logBookId }
        _selectedLogBook.value = selectedLogbook
    }

    fun fetchButtonsForClientAndScreen(clientId: String, screenId: String) {
        // ... (function implementation remains unchanged) ...
        if (clientId.isBlank() || screenId.isBlank()) {
            _buttonConfigs.value = emptyList()
            setErrorMessage("Fehler bei der Button-Konfiguration.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val buttonConfigs =
                    RetrofitInstance.bookingApi.getButtonsForClientAndScreen(clientId, screenId)
                _buttonConfigs.value = buttonConfigs
                Log.d("LogBookViewModel", "Fetched ${buttonConfigs.size} buttons.")
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler beim Laden der Buttons."
                    is HttpException -> "API-Fehler beim Laden der Buttons: HTTP ${e.code()}"
                    else -> "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                }
                setErrorMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    private val _isLoading = MutableStateFlow(false)


    fun fetchPurposeOfTrips() {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val purposeOfTrips = RetrofitInstance.bookingApi.getPurposeOfTrips()
                _purposeOfTrips.value = purposeOfTrips
                Log.d("LogBookViewModel", "Fetched ${purposeOfTrips.size} purpose of trips.")
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler beim Laden der Buttons."
                    is HttpException -> "API-Fehler beim Laden der Buttons: HTTP ${e.code()}"
                    else -> "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                }
                setErrorMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun fetchStatusOptions() {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val statusOptions = RetrofitInstance.bookingApi.getStatusOptions()
                _statusOptions.value = statusOptions
                Log.d("LogBookViewModel", "Fetched ${statusOptions.size} status options.")
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler beim Laden der Buttons."
                    is HttpException -> "API-Fehler beim Laden der Buttons: HTTP ${e.code()}"
                    else -> "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                }
                setErrorMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchVehiclesByDriver(driverId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val vehiclesBYDriverId = RetrofitInstance.logBookApi.getVehiclesByDriverId(driverId)
                _vehiclesBYDriverId.value = vehiclesBYDriverId
                Log.d("BookingViewModel", "Fetched ${vehiclesBYDriverId.size} status options.")
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler beim Laden der Buttons."
                    is HttpException -> "API-Fehler beim Laden der Buttons: HTTP ${e.code()}"
                    else -> "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                }
                setErrorMessage(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleEvent(event: LogBookEvent) {
        when (event) {
            is LogBookEvent.ButtonClicked -> handleButtonClicked(event.config)
            is LogBookEvent.LogbookCheckedChange -> selectLogBook(event.logbookId)
            is LogBookEvent.LogbookSelected -> toggleLogBookChecked(event.logBook)
        }
    }


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

