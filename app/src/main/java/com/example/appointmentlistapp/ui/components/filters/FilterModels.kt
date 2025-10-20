package com.example.appointmentlistapp.ui.components.filters


// Classes que defineixen l'estat i els esdeveniments del filtre
data class BookingFilterState(
    val entryNr: String = "",
    val status: String = "",
    val handOverDate: String = "",
    val travelPurposeChange: String = "",
    val vehicle: String = "",
    val purposeId: String,
    val registrationName: String,
    val purpose: String
)

//Different reactions from the users
sealed class BookingFilterEvent {
    data class BookingNoChange(val bookingNo: String) : BookingFilterEvent()
    data class StatusChange(val status: String) : BookingFilterEvent()
    data class HandOverDateChange(val date: String) : BookingFilterEvent()
    data class TravelPurposeChange(val purpose: String) : BookingFilterEvent()
    data class VehicleChange(val registration: Any) : BookingFilterEvent()
    data class RegistrationName(val registrationName: String) : BookingFilterEvent()

    object ApplyFilter : BookingFilterEvent()
    object ResetFilter : BookingFilterEvent()
}