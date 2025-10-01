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
import kotlinx.coroutines.selects.select
import retrofit2.HttpException
import java.io.IOException

// --- 💡 1. New UI State Data Class ---
data class BookingUiState(
    val bookings: List<Appointment> = emptyList(),
    val selectedBooking: Appointment? = null,
    val buttonConfigs: List<ButtonConfig> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDetails: Boolean = true // UI state managed here
)

// --- 💡 2. New UI Event/Intent Sealed Class ---
sealed class BookingEvent {
    data class ButtonClicked(val config: ButtonConfig) : BookingEvent()
    data class BookingSelected(val booking: Appointment) : BookingEvent()
    data class BookingCheckedChange(val bookingId: String) : BookingEvent()
    // You could also add a ToggleDetails event here if you want it decoupled from buttons
}


class BookingViewModel : ViewModel() {

    val repository = BookingRepository(RetrofitInstance.api)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId
    private val _bookings = MutableStateFlow<List<Appointment>>(emptyList())
    val bookings: MutableStateFlow<List<Appointment>> = _bookings



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
                val driverAppointments = RetrofitInstance.api.getAppointmentsByDriverId(driverId)
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

    // --- 💡 3. CONSOLIDATED UI STATE ---
    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    // Flow from the repository to be collected by the UI (still exists, but can be moved into UiState)
    val allAppointments: StateFlow<List<Appointment>> = repository.getAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initial fetch call is now handled by the UI (BookingScreen) using LaunchedEffect
    }

    // --- 💡 4. EVENT HANDLER (Processes all UI interactions) ---
    fun handleEvent(event: BookingEvent) {
        when (event) {
            is BookingEvent.ButtonClicked -> handleButtonClicked(event.config)
            is BookingEvent.BookingSelected -> selectBooking(event.booking)
            is BookingEvent.BookingCheckedChange -> toggleBookingChecked(event.bookingId)
        }
    }

    private fun handleButtonClicked(config: ButtonConfig) {
        // Find the action type from the database config
        when (config.type.lowercase().trim()) {
            "details" -> _uiState.update {
                it.copy(showDetails = !it.showDetails) // Toggle details pane
            }
            "add" -> Log.d("ViewModel", "Action: Navigate to ADD screen.")
            "edit" -> Log.d("ViewModel", "Action: Navigate to EDIT screen for booking ID: ${uiState.value.selectedBooking?.id}")
            // ... add other dynamic actions (save, cancel, etc.)
            else -> Log.w("ViewModel", "Unknown button action type: ${config.type}")
        }
    }

    // ----------------------------------------------------
    // --- Existing Data Fetch and Logic Functions ---
    // ----------------------------------------------------

    fun fetchButtonsForClientAndScreen(clientId: String, screenId: String) {
        if (clientId.isBlank() || screenId.isNullOrBlank()) {
            _uiState.update { it.copy(buttonConfigs = emptyList()) }
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // API call
                val buttonConfigs = RetrofitInstance.api.getButtonsForClientAndScreen(clientId, screenId)

                // 💡 Update CONSOLIDATED UI STATE
                _uiState.update { it.copy(buttonConfigs = buttonConfigs) }
                Log.d("BookingViewModel", "Fetched ${buttonConfigs.size} buttons.")

            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is IOException -> "Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung."
                    is HttpException -> "API-Fehler: HTTP ${e.code()}"
                    else -> "Ein unerwarteter Fehler ist aufgetreten: ${e.message}"
                }
                setErrorMessage(errorMsg)
                Log.e("BookingViewModel", "Error fetching buttons.", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Other functions (fetchAppointments, insertBooking, deleteBooking) should also be updated
    // to use the _uiState.update { ... } pattern instead of individual flow updates.



    fun toggleBookingChecked(bookingId: String) {
        _uiState.update { currentState ->
            val updatedBookings = currentState.bookings.map { booking ->
                if (booking.id == bookingId) {
                    booking.copy(isChecked = !(booking.isChecked ?: false))
                } else {
                    booking
                }
            }
            currentState.copy(bookings = updatedBookings)
        }
    }

    private fun setErrorMessage(message: String?) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun selectBooking(booking: Appointment) {
        _uiState.update { it.copy(selectedBooking = booking) }
    }

    // ❌ REMOVE the redundant loadButtonsForScreen function if it exists here.
}


