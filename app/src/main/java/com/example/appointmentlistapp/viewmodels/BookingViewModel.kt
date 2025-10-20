package com.example.appointmentlistapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking // Import the UI Model
import com.example.appointmentlistapp.data.PurposeOfTrip
import com.example.appointmentlistapp.data.StatusOption
import com.example.appointmentlistapp.data.Vehicle
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
    data class BookingSelected(val booking: String) : BookingEvent()
    data class BookingCheckedChange(val bookingId: String) : BookingEvent()
}

// --- Conversion Function (Mapper) (Unchanged) ---
private fun convertAppointmentToBooking(appointment: Appointment): Booking {
    // ... (Mapper implementation omitted for brevity) ...
    return Booking(
        // Booking section
        bookingId = appointment.id ?: "0",
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
        purposeOfTrip = (appointment.purposeOfTrip ?: "-") as String,

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
        internNumber = appointment.internalNumber ?: "-",
        processNumber = appointment.processNumber ?: "-",
        id = appointment.id,
        driverId = appointment.driverId,
        appointmentDateTime = appointment.appointmentDateTime,
        vehicleRegistration = appointment.vehicleRegistrationName,
        purposeOfTripId = appointment.purposeOfTripId,
        odometerPickup = appointment.odometerPickup,
        odometerReturn = appointment.odometerReturn,
        createdAt = appointment.createdAt,
        updatedAt = appointment.updatedAt,
        //vehicleId = appointment.vehicleid,
        status = appointment.appointmentStatus,
        vehiclePoolId = appointment.vehiclePoolId,
        driverName = appointment.driverName,
        tripPurposeName = appointment.tripPurposeName,
        vehiclePoolName = appointment.vehiclePoolName,
        vehicleRegistrationName = appointment.vehicleRegistrationName
    )
}

class BookingViewModel : ViewModel() {

    // --- Filter State and Handler ---
    private val _filterState = MutableStateFlow(
        BookingFilterState(
            entryNr = "",
            status = "",
            handOverDate = "",
            travelPurposeChange = "",
            vehicle = "",
            purposeId = "",
            registrationName = "",
            purpose = ""
        )
    )
    val filterState: StateFlow<BookingFilterState> = _filterState.asStateFlow()

    fun handleFilterEvent(event: BookingFilterEvent) {
        when (event) {
            // Aquests 'update' disparen automàticament el filtre 'combine'
            is BookingFilterEvent.BookingNoChange -> _filterState.update { it.copy(entryNr = event.bookingNo) }
            is BookingFilterEvent.StatusChange -> _filterState.update { it.copy(status = event.status) }
            is BookingFilterEvent.HandOverDateChange -> _filterState.update { it.copy(handOverDate = event.date) }
            is BookingFilterEvent.RegistrationName -> _filterState.update { it.copy(registrationName = event.registrationName) }
            is BookingFilterEvent.TravelPurposeChange -> _filterState.update {
                it.copy(
                    travelPurposeChange = event.purpose
                )
            }

            is BookingFilterEvent.VehicleChange -> _filterState.update { it.copy(vehicle = event.registration.toString()) }

            BookingFilterEvent.ApplyFilter -> {
                // CORREGIT: No cal cridar fetchAppointments. El filtre ja és aplicat.
                Log.d("ViewModel", "Filter applied (State updated).")
            }

            BookingFilterEvent.ResetFilter -> {
                _filterState.value = BookingFilterState(
                    entryNr = "",
                    status = "",
                    handOverDate = "",
                    travelPurposeChange = "",
                    vehicle = "",
                    purposeId = "",
                    registrationName = "",
                    purpose = ""
                )
                Log.d("ViewModel", "Filter reset.")
            }
        }
    }

    // --- Data State ---

    private val _purposeOfTrips = MutableStateFlow<List<PurposeOfTrip>>(emptyList())
    val purposeOfTrips: StateFlow<List<PurposeOfTrip>> = _purposeOfTrips.asStateFlow()

    private val _statusOptions = MutableStateFlow<List<StatusOption>>(emptyList())
    val statusOptions: StateFlow<List<StatusOption>> = _statusOptions.asStateFlow()

    private val _vehiclesBYDriverId = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehiclesBYDriverId: StateFlow<List<Vehicle>> = _vehiclesBYDriverId.asStateFlow()
    // Internal flow holding the CURRENTLY filtered *API* models (Appointment)
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())

    // This public flow holds all appointments fetched from the API, without any filtering.
    val allAppointments: StateFlow<List<Appointment>> = _allAppointments.asStateFlow()

    // Public StateFlow for the UI: maps the filtered API models to the UI model (Booking)
    val bookings: StateFlow<List<Booking>> =
        _allAppointments.combine(_filterState) { allAppointments, filter ->
            val filteredAppointments = applyFilterToBookings(allAppointments, filter)
            filteredAppointments.map { convertAppointmentToBooking(it) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appointmentsUI: StateFlow<List<Appointment>> =
        _allAppointments.combine(_filterState) { allAppointments, filter ->
            // Eliminem el .map { convertAppointmentToBooking(it) }
            applyFilterToBookings(allAppointments, filter) // Retorna List<Appointment> directament
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    val isLoading = MutableStateFlow(false)

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
    fun fetchAppointments(
        driverId: String? = _scannedDriverId.value,
        useCachedData: Boolean = false
    ) {
        if (driverId.isNullOrBlank()) {
            _allAppointments.value = emptyList()
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        if (useCachedData) {
            // Apply filter instantly without a network call
            // Data is already cached in _allAppointments, the 'bookings' flow will update automatically.
            return

        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                // 1. Fetch raw data from API and store it
                val appointments =
                    RetrofitInstance.api.getAppointmentsViewByDriverId("FD104CC0-4756-4D24-8BDF-FF06CF716E22")
                _allAppointments.value = appointments

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
    private fun applyFilterToBookings(
        appointments: List<Appointment>,
        filter: BookingFilterState
    ): List<Appointment> {

        // Si no hi ha cap filtre actiu, retornem la llista sencera
        if (filter.entryNr.isBlank() && filter.status.isNullOrBlank() && filter.vehicle.isBlank() &&
            filter.handOverDate.isNullOrBlank() && filter.travelPurposeChange == ""
        ) {
            return appointments
        }

        return appointments.filter { appointment ->
            // 1. Vorgangsnr. (Booking No)
            val matchesBookingNo = filter.entryNr.isBlank() ||
                    appointment.processNumber.orEmpty()
                        .contains(filter.entryNr, ignoreCase = true)


            // 2. Status (El teu filtre per estat!)
            val matchesStatus = filter.status.isBlank() ||
                    appointment.appointmentStatus.orEmpty()
                        .contains(filter.status, ignoreCase = true)

            // 3. Date
            val matchesDate = filter.handOverDate.isNullOrBlank() ||
                    appointment.handOverDate.orEmpty()
                        .contains(filter.handOverDate, ignoreCase = true)


            val matchesVehicle = filter.registrationName == "" ||
                    appointment.vehicleRegistrationName == filter.registrationName


            // 4. Purpose
            // The filter state uses travelval matchesVehicle = filter.registrationName == "" ||
            //                    appointment.vehicleRegistrationName == filter.registrationNameval matchesVehicle = filter.registrationName == "" ||
            //                    appointment.vehicleRegistrationName == filter.registrationNamePurposeChange (Int), not purposeId (String)
            val matchesPurpose = filter.travelPurposeChange == "" ||
                    appointment.tripPurposeName == filter.travelPurposeChange

            //TO-DO SERGI -> only filters first by purpose to match the vehicle KFZ, investigate
            // Combine all conditions. An appointment must match all active filters.
            matchesBookingNo && matchesStatus && matchesDate &&  (matchesPurpose && matchesVehicle )
        }
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
        // 1. Actualitza el valor del Flow mestre, creant una nova llista
        _allAppointments.value = _allAppointments.value.map { appointment ->
            if (appointment.id == bookingId) {
                appointment.copy(isChecked = !appointment.isChecked)
            } else {
                appointment
            }
        }
        // La línia anterior dispara automàticament el filtre 'combine'.
    }

    private fun handleButtonClicked(config: ButtonConfig) {
        when (config.type.lowercase().trim()) {
            "details" -> _showDetails.value = !_showDetails.value
            "add" -> Log.d("ViewModel", "Action: Navigate to ADD screen.")
            "edit" -> Log.d(
                "ViewModel",
                "Action: Navigate to EDIT screen for booking ID: ${_selectedBooking.value?.bookingId}"
            )

            else -> Log.w("ViewModel", "Unknown button action type: ${config.type}")
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    private val _isLoading = MutableStateFlow(false)

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
                    RetrofitInstance.api.getButtonsForClientAndScreen(clientId, screenId)
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


    fun fetchPurposeOfTrips() {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val purposeOfTrips = RetrofitInstance.api.getPurposeOfTrips()
                _purposeOfTrips.value = purposeOfTrips
                Log.d("BookingViewModel", "Fetched ${purposeOfTrips.size} purpose of trips.")
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
                val statusOptions = RetrofitInstance.api.getStatusOptions()
                _statusOptions.value = statusOptions
                Log.d("BookingViewModel", "Fetched ${statusOptions.size} status options.")
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
                val vehiclesBYDriverId = RetrofitInstance.api.getVehiclesByDriverId(driverId)
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




    fun selectBooking(bookingId: String) {
        val selectedAppointment = _allAppointments.value.find { it.id == bookingId }
        if (selectedAppointment != null) {
            _selectedBooking.value = convertAppointmentToBooking(selectedAppointment)
        }
    }
}


