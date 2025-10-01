package com.example.appointmentlistapp.data.model

import com.example.appointmentlistapp.Driver

 data class Appointment(
    val id: String,
    val driverId: String,
    val driver: Driver?,
    val appointmentDateTime: String,
    val status: String,
    val description: String,
    val vehicleRegistration: String? = null,
    val vehiclePool: String? = null,
    val purposeOfTrip: String? = null,
    val bookingDate: String? = null, // Using String for DATETIME/DATE fields
    val pickupTime: String? = null,
    val returnDate: String? = null,
    val returnTime: String? = null,

    // Location and Odometer
    val pickupLocation: String? = null,
    val returnLocation: String? = null,
    val odometerPickup: String? = null, // Using String/Decimal for numerical fields
    val odometerReturn: String? = null,
    val distance: String? = null,

    // Administrative/Cancellation
    val cancellationDate: String? = null,
    val cancellationReason: String? = null,
    val note: String? = null,

    // Core flags and timestamps
    var isChecked: Boolean = false,
    var createdAt: String,
    var updatedAt: String

)