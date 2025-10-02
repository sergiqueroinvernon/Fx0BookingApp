package com.example.appointmentlistapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking // Import the UI Model
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment // Import the API Model
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// --- UI Event/Intent Sealed Class ---
sealed class BookingEvent {
    data class ButtonClicked(val config: ButtonConfig) : BookingEvent()
    // The event carries the API model (Appointment)
    data class BookingSelected(val booking: Appointment) : BookingEvent()
    data class BookingCheckedChange(val bookingId: String) : BookingEvent()
}

// --- Conversion Function (Mapper) ---
private fun convertAppointmentToBooking(appointment: Appointment): Booking {
    // This function is CORRECTLY implemented to map from Appointment to Booking.
    // It provides safe defaults ("-", "N/A", etc.) for non-nullable Booking fields.
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
    )
}


class BookingViewModel : ViewModel() {

    val repository = BookingRepository(RetrofitInstance.api)

    // --- INDIVIDUAL STATE FLOWS ---
    // ✅ FIX 1: Change the selected item type to the UI Model (Booking)
    private val _selectedBooking = MutableStateFlow<Booking?>(null)
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow() // Expose Booking?

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId.asStateFlow()

    private val _bookings = MutableStateFlow<List<Appointment>>(emptyList())
    val bookings: StateFlow<List<Appointment>> = _bookings.asStateFlow()

    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    val buttonConfigs: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()

    private val _showDetails = MutableStateFlow(true)
    val showDetails: StateFlow<Boolean> = _showDetails.asStateFlow()


    fun setScannedDriverId(driverId: String) {
        if (_scannedDriverId.value != driverId) {
            _scannedDriverId.value = driverId
            fetchAppointments(driverId)
        }
    }

    fun fetchAppointments(driverId: String? = _scannedDriverId.value) {
        if (driverId.isNullOrBlank()) {
            _bookings.value = emptyList()
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                // Assuming API returns List<Appointment>
                val driverAppointments = RetrofitInstance.api.getAppointmentsByDriverId("FD104CC0-4756-4D24-8BDF-FF06CF716E22")
                _bookings.value = driverAppointments
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

    fun handleEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.ButtonClicked -> handleButtonClicked(event.config)
            is BookingEvent.BookingSelected -> selectBooking(event.booking)
            is BookingEvent.BookingCheckedChange -> toggleBookingChecked(event.bookingId)
        }
    }

    private fun handleButtonClicked(config: ButtonConfig) {
        when (config.type.lowercase().trim()) {
            "details" -> _showDetails.value = !_showDetails.value // Toggle the detail pane
            "add" -> Log.d("ViewModel", "Action: Navigate to ADD screen.")
            // Use the correct Booking ID when the selectedBooking is now a Booking object:
            "edit" -> Log.d("ViewModel", "Action: Navigate to EDIT screen for booking ID: ${_selectedBooking.value?.bookingId}")
            else -> Log.w("ViewModel", "Unknown button action type: ${config.type}")
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    fun fetchButtonsForClientAndScreen(clientId: String, screenId: String) {
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

    // ✅ FIX 2: This function is now correctly using the conversion and assigning the right type
    fun selectBooking(appointment: Appointment) {
        // Convert the API model (Appointment) to the UI model (Booking)
        _selectedBooking.value = convertAppointmentToBooking(appointment)
    }

    fun toggleBookingChecked(bookingId: String) {
        _bookings.value = _bookings.value.map { appointment ->
            if (appointment.id == bookingId) {
                // isChecked is non-nullable Boolean in Appointment
                appointment.copy(isChecked = !appointment.isChecked)
            } else {
                appointment
            }
        }
    }
}