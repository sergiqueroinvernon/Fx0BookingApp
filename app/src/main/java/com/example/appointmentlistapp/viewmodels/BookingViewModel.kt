// In ui/viewmodel/BookingViewModel.kt
package com.example.appointmentlistapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appointmentlistapp.data.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel : ViewModel() {


    private val _bookings = MutableStateFlow(

        // This holds the original, unfiltered list of all bookings




        // Populated list with more sample bookings
        listOf(
            Booking(
                transactionId = "1000117078",
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
                isChecked = false
            ),
            // --- ADDED EXAMPLES ---
            Booking(
                transactionId = "1000016973",
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
                isChecked = true
            ),
            Booking(
                transactionId = "1000016853",
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
                isChecked = false
            ),
            Booking(
                transactionId = "1000013976",
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
                isChecked = false
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
            if (booking.transactionId == bookingId) {
                booking.copy(isChecked = !booking.isChecked)
            } else {
                booking
            }
        }
    }
}