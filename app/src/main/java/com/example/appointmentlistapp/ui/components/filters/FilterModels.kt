package com.example.appointmentlistapp.ui.components.filters


// Classes que defineixen l'estat i els esdeveniments del filtre
data class BookingFilterState(
    val bookingNo: String = "",
    val status: String = "",
    val handOverDate: String = "",
    val travelPurposeChange: Int = 0,
    val vehicle: String = "",
    val purposeId: String
)

sealed class BookingFilterEvent {
    data class BookingNoChange(val bookingNo: String) : BookingFilterEvent()
    data class StatusChange(val status: String) : BookingFilterEvent()
    data class HandOverDateChange(val date: String) : BookingFilterEvent()
    data class TravelPurposeChange(val purposeId: Int) : BookingFilterEvent()
    data class VehicleChange(val registration: String) : BookingFilterEvent()
    object ApplyFilter : BookingFilterEvent()
    object ResetFilter : BookingFilterEvent()
}