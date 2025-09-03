package com.example.appointmentlistapp.data

data class Booking(
    // Booking section
    val transactionId: String,
    val status: String,
    val driver: String,

    // Trip details section
    val pickupDate: String,
    val pickupTime: String,
    val returnDate: String,
    val returnTime: String,
    val vehicle: String,
    val vehiclePool: String,
    val purposeOfTrip: String,
    val pickupLocation: String,
    val returnLocation: String,
    val odometerReadingPickup: String,
    val odometerReadingReturn: String,
    val distance: String,

    // Cancellation section (nullable for when there is no cancellation)
    val cancellationDate: String?,
    val cancellationReason: String?,

    // Notes section (nullable)
    val note: String?,
    var isChecked: Boolean = false
)