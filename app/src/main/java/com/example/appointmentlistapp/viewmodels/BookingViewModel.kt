// In ui/viewmodel/BookingViewModel.kt
package com.example.appointmentlistapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.Booking
import com.example.appointmentlistapp.data.BookingRepository
import com.example.appointmentlistapp.data.api.BookingApiModel
import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
* VidewModel for the BookingScreen.
* It manages the UI state and handles the logic for fetching button configuration from the repository
* */
class BookingViewModel(private val repository: BookingRepository) : ViewModel()

{


    // A private, mutable StateFlow to hold the list of buttons
    private val _buttons = MutableStateFlow<List<ButtonConfig>>(emptyList())
    // A public, read-only StateFlow that the UI can observe
    val buttons: StateFlow<List<ButtonConfig>> = _buttons

    /*Loads the button configurations for a specific client and screen.
    * This function should be called from the UI when a user navigate toa new screen
    *
    * It launches a coroutine to collect the Flow from the repository
    * Any change in the database will automatically update this StateFlow,
    * triggering a recomposition in the UI
    * @param clientId the ID of the client
    * @param screenId the ID of the screen
    * */

    fun loadButtonsForClientAndScreen(clientId: String, screenId: String) {
        viewModelScope.launch {
            //Collect the Flow from the repository
            repository.getButtonsForClientsAndScreen(clientId, screenId).collect {
                //Update the state of the ViewModel
                    buttonConfigs ->
                _buttons.value = buttonConfigs
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
                booking.copy(isChecked = !booking.isChecked)
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
            repository.getAppointments(booking)
        }
    }


}