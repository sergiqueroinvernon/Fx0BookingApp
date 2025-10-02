package com.example.appointmentlistapp.ui.components.filters


// Classes que defineixen l'estat i els esdeveniments del filtre
data class BookingFilterState(
    val bookingNo: String = "",
    val status: String = "",
    val handOverDate: String = "",
    val travelPurpose: String = "",
    val vehicle: String = ""
)

sealed class BookingFilterEvent {
    data class BookingNoChange(val bookingNo: String) : BookingFilterEvent()
    data class StatusChange(val status: String) : BookingFilterEvent()
    data class HandOverDateChange(val date: String) : BookingFilterEvent()
    data class TravelPurposeChange(val purpose: String) : BookingFilterEvent()
    data class VehicleChange(val vehicle: String) : BookingFilterEvent()
    object ApplyFilter : BookingFilterEvent()
    object ResetFilter : BookingFilterEvent()
}