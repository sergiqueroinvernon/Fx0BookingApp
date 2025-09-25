// In ui/viewmodel/BookingViewModel.kt
package com.example.appointmentlistapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// FIX: The ViewModel must have a constructor that accepts a BookingRepository.
// The factory will provide this dependency.
class BookingViewModel(private val repository: BookingRepository) : ViewModel() {

    // Private StateFlows to hold the data
    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    private val _bookings = MutableStateFlow<List<Booking>>(
        // FIX: Remove hardcoded data. It should come from the repository.
        emptyList()
    )
    private val _selectedBooking = MutableStateFlow<Booking?>(null)

    // Public StateFlows that the UI can observe
    val buttonsConfig: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // FIX: This flow should use the repository to get the list of appointments
    val allAppointments: StateFlow<List<Appointment>> = repository.getAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // You can start loading initial data here
    }

    fun loadButtonsForScreen(clientId: String, screenId: String) {
        if (clientId.isBlank() || screenId.isBlank()) {
            setErrorMessage("Client-ID oder Bildschirm-ID fehlt.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // FIX: Use the repository method to get data
                repository.getButtonsForClientAndScreen(clientId, screenId)
                    .collect { buttons ->
                        _buttonConfigs.value = buttons
                    }
            } catch (e: IOException) {
                setErrorMessage("Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung")
            } catch(e: HttpException) {
                setErrorMessage("API-Fehler: ${e.message()}")
            } catch (e: Exception) {
                setErrorMessage("Ein unerwarteter Fehler ist aufgetreten: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // FIX: Renamed for clarity and to avoid confusion with loadButtonsForScreen
    fun fetchAppointments(driverId: String?) {
        if (driverId.isNullOrBlank()) {
            setErrorMessage("Fahrer-ID fehlt.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // FIX: Use the repository to get data
                val appointments = repository.getAppointmentsByDriver(driverId)
                // Assuming you have a way to update your bookings state from Appointments
                // _bookings.value = appointments
            } catch (e: Exception) {
                setErrorMessage("Ein Fehler ist beim Abrufen der Termine aufgetreten: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertBooking(booking: Booking) {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                repository.createAppointment(booking)
                // FIX: After a successful insert, you should refresh the data shown in the UI.
                // This is a simple example; consider a single source of truth for your data.
                // refreshBookings()
            } catch (e: Exception) {
                setErrorMessage("Fehler beim Einfügen der Buchung: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                repository.deleteAppointment(booking)
                // FIX: After a successful delete, you should refresh the data shown in the UI.
                // refreshBookings()
            } catch (e: Exception) {
                setErrorMessage("Fehler beim Löschen der Buchung: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectBooking(booking: Booking) {
        _selectedBooking.value = booking
    }

    fun toggleBookingChecked(bookingId: String) {
        _bookings.value = _bookings.value.map { booking ->
            if (booking.bookingId == bookingId) {
                booking.copy( isChecked = !booking.isChecked!!,)
            } else {
                booking
            }
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}