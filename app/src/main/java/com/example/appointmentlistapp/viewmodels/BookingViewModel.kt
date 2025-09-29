package com.example.appointmentlistapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BookingViewModel : ViewModel() {

    public val repository = BookingRepository(RetrofitInstance.api)

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
    }

    fun fetchButtonsForClientAndScreen(clientId: String, screenId: String) {
        if (clientId.isBlank() || screenId.isNullOrBlank()) {
            _buttonConfigs.value = emptyList()
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                // API call: This line should update the StateFlow on success
                val buttonConfigs = RetrofitInstance.api.getButtonsForClientAndScreen(clientId, screenId)

                // Log the size to confirm data was received successfully
                Log.d("BookingViewModel", "Fetched ${buttonConfigs.size} buttons.")

                _buttonConfigs.value = buttonConfigs
            } catch (e: IOException) {
                setErrorMessage("Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und stellen Sie sicher, dass die API läuft.")
                Log.e("BookingViewModel", "Network error while fetching buttons.", e)
            } catch (e: HttpException) {
                setErrorMessage("API-Fehler: HTTP ${e.code()}")
                Log.e("BookingViewModel", "HTTP error while fetching buttons: ${e.code()}", e)
            } catch (e: Exception) {
                setErrorMessage("Ein unerwarteter Fehler ist aufgetreten: ${e.message}")
                Log.e("BookingViewModel", "Unexpected error while fetching buttons.", e)
            } finally {
                _isLoading.value = false
            }
        }
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