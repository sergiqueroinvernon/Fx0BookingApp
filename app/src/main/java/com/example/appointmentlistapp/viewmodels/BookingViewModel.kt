package com.example.appointmentlistapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking // Import the UI Model
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment // Import the API Model
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import com.example.appointmentlistapp.ui.components.filters.BookingFilterState
import com.example.appointmentlistapp.ui.components.filters.BookingFilterEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// --- UI Event/Intent Sealed Class (Unchanged) ---
sealed class BookingEvent {
    data class ButtonClicked(val config: ButtonConfig) : BookingEvent()
    data class BookingSelected(val booking: Appointment) : BookingEvent()
    data class BookingCheckedChange(val bookingId: String) : BookingEvent()
}

// --- Conversion Function (Mapper) (Unchanged) ---
private fun convertAppointmentToBooking(appointment: Appointment): Booking {
    // ... (Mapper implementation omitted for brevity) ...
    return Booking(
        // Booking section
        bookingId = appointment.id ?: "0",
        status = appointment.status ?: "Unknown",
        driver = appointment.driver?.name ?: "-",
        bookingDate = appointment.appointmentDateTime ?: "-",
        description = appointment.description ?: "N/A",

        // --- Trip details section ---
        pickupDate = appointment.bookingDate ?: "-",
        pickupTime = appointment.pickupTime ?: "-",
        returnDate = appointment.returnDate ?: "-",
        returnTime = appointment.returnTime ?: "-",
        vehicle = appointment.vehicleRegistration ?: "-",
        vehiclePool = appointment.vehiclePool ?: "-",
        purposeOfTrip = appointment.purposeOfTrip ?: "-",

        pickupLocation = appointment.pickupLocation ?: "-",
        returnLocation = appointment.returnLocation ?: "-",

        // Odometer mapping
        odometerReadingPickup = appointment.odometerPickup ?: "-",
        odometerReadingReturn = appointment.odometerReturn ?: "-",
        distance = appointment.distance ?: "-",

        // Cancellation section
        cancellationDate = appointment.cancellationDate ?: "-",
        cancellationReason = appointment.cancellationReason ?: "-",

        // Notes section
        note = appointment.note ?: "-",
        isChecked = appointment.isChecked,
        handOverDate = appointment.handOverDate ?: "-",
        internNumber = appointment.internNumber ?: "-",
    )
}

class BookingViewModel : ViewModel() {

    // --- Filter State and Handler ---
    private val _filterState = MutableStateFlow(BookingFilterState())
    val filterState: StateFlow<BookingFilterState> = _filterState.asStateFlow()

    fun handleFilterEvent(event: BookingFilterEvent) {
        when (event) {
            is BookingFilterEvent.BookingNoChange -> _filterState.update { it.copy(bookingNo = event.bookingNo) }
            is BookingFilterEvent.StatusChange -> _filterState.update { it.copy(status = event.status) }
            is BookingFilterEvent.HandOverDateChange -> _filterState.update { it.copy(handOverDate = event.date) }
            is BookingFilterEvent.TravelPurposeChange -> _filterState.update { it.copy(travelPurpose = event.purpose) }
            is BookingFilterEvent.VehicleChange -> _filterState.update { it.copy(vehicle = event.vehicle) }

            BookingFilterEvent.ApplyFilter -> {
                Log.d("ViewModel", "Applying filter: ${_filterState.value}")
                // Trigger appointment fetching to apply filter to list
                fetchAppointments(useCachedData = true) // Use cached data after initial fetch
            }
            BookingFilterEvent.ResetFilter -> {
                _filterState.value = BookingFilterState()
                Log.d("ViewModel", "Filter reset.")
                // Trigger appointment fetching to refresh list without filter
                fetchAppointments(useCachedData = true) // Use cached data after initial fetch
            }
        }
    }

    // --- Data State ---

    // Store the raw, unfiltered data from the API (mutable list)
    private var _allAppointments = emptyList<Appointment>()

    // Internal flow holding the CURRENTLY filtered *API* models (Appointment)
    private val _filteredAppointments = MutableStateFlow<List<Appointment>>(emptyList())

    // Public StateFlow for the UI: maps the filtered API models to the UI model (Booking)
    val bookings: StateFlow<List<Appointment>> = _filteredAppointments
        .map { appointments ->
            appointments.map(::convertAppointmentToBooking)
        }
        .stateIn(
            scope = viewModelScope,
            // Adjust to fit your lifecycle needs
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId.asStateFlow()

    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    val buttonConfigs: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()

    private val _showDetails = MutableStateFlow(true)
    val showDetails: StateFlow<Boolean> = _showDetails.asStateFlow()


    fun setScannedDriverId(driverId: String) {
        if (_scannedDriverId.value != driverId) {
            _scannedDriverId.value = driverId
            fetchAppointments()
        }
    }

    // 🆕 MODIFIED: Function now checks if it should fetch new data or use cached data for filtering
    fun fetchAppointments(driverId: String? = _scannedDriverId.value, useCachedData: Boolean = false) {
        if (driverId.isNullOrBlank()) {
            _filteredAppointments.value = emptyList()
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        if (useCachedData) {
            // Apply filter instantly without a network call
            applyFilterToBookings(_allAppointments, _filterState.value)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                // 1. Fetch raw data from API and store it
                _allAppointments = RetrofitInstance.api.getAppointmentsByDriverId("FD104CC0-4756-4D24-8BDF-FF06CF716E22")

                // 2. Apply current filter to the newly fetched list
                applyFilterToBookings(_allAppointments, _filterState.value)

            } catch (e: IOException) {
                setErrorMessage("Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und stellen Sie sicher, dass die API läuft.")
            } catch (e: HttpException) {
                setErrorMessage("API-Fehler: ${e.message()}")
            } catch (e: Exception) {
                setErrorMessage("Ein unerwarteter Fehler ist aufgetreten: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🆕 NEW: Dedicated function to apply the filter logic
    private fun applyFilterToBookings(appointments: List<Appointment>, filter: BookingFilterState) {
        val filteredList = appointments.filter { appointment ->
            // --- Filtering Logic ---
            val matchesBookingNo = filter.bookingNo.isBlank() ||
                    appointment.id.orEmpty().contains(filter.bookingNo, ignoreCase = true)

            val matchesStatus = filter.status.isBlank() ||
                    appointment.status.orEmpty().contains(filter.status, ignoreCase = true)

            val matchesDate = filter.handOverDate.isBlank() ||
                    appointment.handOverDate.orEmpty().contains(filter.handOverDate, ignoreCase = true)

            val matchesPurpose = filter.travelPurpose.isBlank() ||
                    appointment.purposeOfTrip.orEmpty().contains(filter.travelPurpose, ignoreCase = true)

            val matchesVehicle = filter.vehicle.isBlank() ||
                    appointment.vehicleRegistration.orEmpty().contains(filter.vehicle, ignoreCase = true)

            matchesBookingNo && matchesStatus && matchesDate && matchesPurpose && matchesVehicle
        }
        // Update the internal flow, which triggers the 'bookings' flow map
        _filteredAppointments.value = filteredList
    }


    fun handleEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.ButtonClicked -> handleButtonClicked(event.config)
            is BookingEvent.BookingSelected -> selectBooking(event.booking)
            is BookingEvent.BookingCheckedChange -> toggleBookingChecked(event.bookingId)
        }
    }

    // 🆕 MODIFIED: Update the ALL appointments list, then re-apply filter to update UI
    fun toggleBookingChecked(bookingId: String) {
        // 1. Update the original source of data (_allAppointments)
        _allAppointments = _allAppointments.map { appointment ->
            if (appointment.id == bookingId) {
                // isChecked is non-nullable Boolean in Appointment
                appointment.copy(isChecked = !appointment.isChecked)
            } else {
                appointment
            }
        }
        // 2. Re-apply the current filter to force an update to _filteredAppointments
        applyFilterToBookings(_allAppointments, _filterState.value)
    }

    private fun handleButtonClicked(config: ButtonConfig) {
        when (config.type.lowercase().trim()) {
            "details" -> _showDetails.value = !_showDetails.value
            "add" -> Log.d("ViewModel", "Action: Navigate to ADD screen.")
            "edit" -> Log.d("ViewModel", "Action: Navigate to EDIT screen for booking ID: ${_selectedBooking.value?.bookingId}")
            else -> Log.w("ViewModel", "Unknown button action type: ${config.type}")
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
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
                val buttonConfigs = RetrofitInstance.api.getButtonsForClientAndScreen(clientId, screenId)
                _buttonConfigs.value = buttonConfigs
                Log.d("BookingViewModel", "Fetched ${buttonConfigs.size} buttons.")
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

    fun selectBooking(appointment: Appointment) {
        // Convert the API model (Appointment) to the UI model (Booking)
        _selectedBooking.value = convertAppointmentToBooking(appointment)
    }
}