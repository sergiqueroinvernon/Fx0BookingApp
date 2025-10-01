package com.example.appointmentlistapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// --- UI Event/Intent Sealed Class ---
sealed class BookingEvent {
    data class ButtonClicked(val config: ButtonConfig) : BookingEvent()
    // NOTE: This should match the type expected by BookingDetails (assuming Booking)
    data class BookingSelected(val booking: Appointment) : BookingEvent()
    data class BookingCheckedChange(val bookingId: String) : BookingEvent()
}

private fun convertAppointmentToBooking(appointment: Appointment): Booking {

    return Booking(
        bookingId = appointment.id ?: "",
        status = appointment.status ?: "",
        driver = appointment.driver?.name ?: "",
        bookingDate = appointment.appointmentDateTime ?: "",
        pickupDate = TODO(),
        returnDate = appointment.returnDate ?: "",
        returnTime = appointment.returnTime ?: "",
        vehicle = TODO(),
        vehiclePool = TODO(),
        purposeOfTrip = TODO(),
        pickupLocation = TODO(),
        returnLocation = TODO(),
        odometerReadingPickup = TODO(),
        odometerReadingReturn = TODO(),
        distance = TODO(),
        cancellationDate = TODO(),
        cancellationReason = TODO(),
        note = TODO(),
        isChecked = TODO(),
        pickupTime = TODO(),
        description = TODO(),
    )

}



class BookingViewModel : ViewModel() {

    val repository = BookingRepository(RetrofitInstance.api)

    // --- INDIVIDUAL STATE FLOWS ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId.asStateFlow()

    // List of appointments/bookings for the Master Pane
    private val _bookings = MutableStateFlow<List<Appointment>>(emptyList())
    val bookings: StateFlow<List<Appointment>> = _bookings.asStateFlow()

    // Currently selected booking for the Detail Pane
    private val _selectedBooking = MutableStateFlow<Appointment?>(null)
    val selectedBooking: StateFlow<Appointment?> = _selectedBooking.asStateFlow()

    // Dynamically loaded buttons
    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    val buttonConfigs: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()

    // Visibility state for the Detail Pane
    private val _showDetails = MutableStateFlow(true)
    val showDetails: StateFlow<Boolean> = _showDetails.asStateFlow()

    // --- Removed all prior conflicting _uiState logic ---

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
            "edit" -> Log.d("ViewModel", "Action: Navigate to EDIT screen for booking ID: ${_selectedBooking.value?.id}")
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

    fun selectBooking(booking: Appointment) {
        _selectedBooking.value = booking
    }

    fun toggleBookingChecked(bookingId: String) {
        _bookings.value = _bookings.value.map { appointment ->
            if (appointment.id == bookingId) {
                appointment.copy(isChecked = !(appointment.isChecked ?: false))
            } else {
                appointment
            }
        }
    }
}