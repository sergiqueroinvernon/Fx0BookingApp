
package com.example.appointmentlistapp.ui.components.filters

// Data class to hold the current state of the Logbook filter (aligned with UI fields)

data class LogBookFilterState(
    // Eintragsnr.
    val entryNr: String = "",
    // Status
    val status: String = "",
    // Datum Start (renamed from handOverDate for clarity, assuming it's a single date or date range start)
    val dateStart: String = "",
    // Reisezweck (using ID/Code for internal logic)
    val vehicleRegistration: String = "",

    val purpose: String = "",
    // Fahrzeug (using the registration name for the display and filtering)

)

// Sealed class defining all possible actions that modify the filter state or trigger an action
sealed class LogBookFilterEvent {
    // Corresponds to the 'Eintragsnr.' field
    data class EntryNrChange(val entryNo: String) : LogBookFilterEvent()

    // Corresponds to the 'Status' dropdown
    data class StatusChange(val status: String) : LogBookFilterEvent()

    // Corresponds to the 'Datum Start' field
    data class DateStartChange(val date: String) : LogBookFilterEvent()

    // Corresponds to the 'Reisezweck' dropdown (using String for ID consistency)
    data class PurposeIdChange(val purposeId: Int) : LogBookFilterEvent()

    // Corresponds to the 'Fahrzeug' dropdown
    data class VehicleRegistrationChange(val registrationName: String) : LogBookFilterEvent()

    // Corresponds to the 'Aktualisieren' button
    object ApplyFilter : LogBookFilterEvent()

    // Corresponds to the 'Zurücksetzen' button
    object ResetFilter : LogBookFilterEvent()
}