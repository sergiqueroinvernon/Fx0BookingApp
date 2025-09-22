// In ui/viewmodel/BookingViewModel.kt
package com.example.appointmentlistapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.api.BookingApiModel
import com.example.appointmentlistapp.data.components.ButtonConfig
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/*
* VidewModel for the BookingScreen.
* It manages the UI state and handles the logic for fetching button configuration from the repository
* */
class BookingViewModel(private val repository: BookingRepository) : ViewModel() {
    // A private, mutable StateFlow to hold the list of buttons
    private val _buttonConfigs = MutableStateFlow<List<ButtonConfig>>(emptyList())

    // A public, read-only StateFlow that the UI can observe
    val buttonsConfig: StateFlow<List<ButtonConfig>> = _buttonConfigs.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadButtonsForScreen(clientId: String, screenId: String) {
        viewModelScope.launch {
            //Collect the Flow from the repository
            try{
                // Access the companion object method correctly
                val buttons = repository.getButtonsForClientsAndScreen(clientId, screenId)
                // If getButtonsForClientAndScreen is not a companion object method,
                // and assuming it's an instance method of BookingRepository,
                // you would call it like this:
                // val buttons = repository.getButtonsForClientAndScreen(clientId, screenId)
                _buttonConfigs.value = buttons as List<ButtonConfig>
            } catch (e: Exception) {

            }

        }
    }

    // Fetch buttons
    private fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }


    fun fetchButtons(driverId: String?, screenId: String) {
        if (driverId.isNullOrBlank() || screenId.isNullOrBlank()) {
            _buttonConfigs.value = emptyList()
            setErrorMessage("Fahrer-ID oder Bildschirm-ID fehlt.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val buttons = RetrofitInstance.api.getAppointmentsByDriverId(driverId)
                // _appointments.value = driverAppointments // Assuming _appointments is defined elsewhere or this is a placeholder
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







    private val _bookings = MutableStateFlow(

        // This holds the original, unfiltered list of all bookings


        // Populated list with more sample bookings
        listOf(
            Booking(
                bookingId = "1000117078",
                status = "Bereit zur Übergabe",
                driver = "Schulz-Eltern, Torsten",
                pickupDate = "20.02.2025",
                pickupTime = "13:15",
                returnDate = "20.02.2025",
                returnTime = "17:00",
                vehicle = "D-EM 711E",
                vehiclePool = "SWD-HW 200",
                purposeOfTrip = "Baustelle",
                pickupLocation = "Hauptstraße 1, Düsseldorf",
                returnLocation = "Hauptstraße 1, Düsseldorf",
                odometerReadingPickup = "12345 km",
                odometerReadingReturn = "12500 km",
                distance = "155 km",
                cancellationDate = null,
                cancellationReason = null,
                note = "Fahrzeug bitte volltanken.",
                isChecked = false,
                bookingDate = TODO()
            ),
            // --- ADDED EXAMPLES ---
            Booking(
                bookingId = "1000016973",
                status = "Storniert",
                driver = "Schulz-Eltern, Torsten",
                pickupDate = "13.02.2025",
                pickupTime = "15:00",
                returnDate = "13.02.2025",
                returnTime = "19:00",
                vehicle = "D-EM 719",
                vehiclePool = "SWD HW 200",
                purposeOfTrip = "Dienstreise",
                pickupLocation = "Nebenweg 5, Köln",
                returnLocation = "Nebenweg 5, Köln",
                odometerReadingPickup = "22100 km",
                odometerReadingReturn = "22100 km",
                distance = "0 km",
                cancellationDate = "10.02.2025",
                cancellationReason = "Termin abgesagt",
                note = null,
                isChecked = true,
                bookingDate = TODO()

            ),
            Booking(
                bookingId = "1000016853",
                status = "Bereit zur Übergabe",
                driver = "Schulz-Eltern, Torsten",
                pickupDate = "06.02.2025",
                pickupTime = "11:00",
                returnDate = "06.02.2025",
                returnTime = "15:00",
                vehicle = "D-WG 450",
                vehiclePool = "SWD HW 200",
                purposeOfTrip = "Baustelle",
                pickupLocation = "Baustelle B1, Essen",
                returnLocation = "Hauptstraße 1, Düsseldorf",
                odometerReadingPickup = "54321 km",
                odometerReadingReturn = "54400 km",
                distance = "79 km",
                cancellationDate = null,
                cancellationReason = null,
                note = null,
                isChecked = false,
                bookingDate = TODO()

            ),
            Booking(
                bookingId = "1000013976",
                status = "Nicht verfügbar",
                driver = "Schulz-Eltern, Torsten",
                pickupDate = "23.05.2024",
                pickupTime = "09:00",
                returnDate = "23.05.2024",
                returnTime = "13:00",
                vehicle = "D-EM 313",
                vehiclePool = "SWD HW 200",
                purposeOfTrip = "Baustelle",
                pickupLocation = "Südring 20, Wuppertal",
                returnLocation = "Südring 20, Wuppertal",
                odometerReadingPickup = "33210 km",
                odometerReadingReturn = "33290 km",
                distance = "80 km",
                cancellationDate = null,
                cancellationReason = null,
                note = "Fahrzeug in Werkstatt.",
                isChecked = false,
                bookingDate = TODO()

            )
        )
    )
    //Private state for the current filter values


    val bookings: StateFlow<List<Booking>> = _bookings

    private val _selectedBooking = MutableStateFlow<Booking?>(_bookings.value.firstOrNull())
    val selectedBooking: StateFlow<Booking?> = _selectedBooking

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


    val allBookings: StateFlow<List<BookingApiModel>> = repository.getAppointments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertBooking(booking: Booking) {
        viewModelScope.launch {
            repository.createAppointment(booking)
        }
    }

    fun deleteBooking(booking: Booking) {
        viewModelScope.launch {
            repository.deleteAppointment(booking)
        }
    }


}