package com.example.appointmentlistapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val repository: BookingRepository
) : ViewModel() {

    // Private MutableStateFlows to hold the data
    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    private val _selectedBooking = MutableStateFlow<Booking?>(null)

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    // Public StateFlows that the UI can observe
    val buttonsConfig: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()
    val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Flow from the repository to be collected by the UI
    val allAppointments: StateFlow<List<Appointment>> = repository.getAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initialization logic here
    }

    fun loadButtonsForScreen(clientId: String, screenId: String) {
        if (clientId.isBlank() || screenId.isBlank()) {
            setErrorMessage("Client-ID oder Bildschirm-ID fehlt.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
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

    fun fetchAppointments(driverId: String?) {
        if (driverId.isNullOrBlank()) {
            setErrorMessage("Fahrer-ID fehlt.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val appointments = repository.getAppointmentsByDriver(driverId)
                // Logic to process appointments and update _bookings should go here
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
                // Consider calling fetchAppointments or implementing a data refresh mechanism
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
                // Consider calling fetchAppointments or implementing a data refresh mechanism
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
                // Assuming isChecked is a non-null property, otherwise handle nullability
                booking.copy( isChecked = !(booking.isChecked ?: false),)
            } else {
                booking
            }
        }
    }

    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }
}