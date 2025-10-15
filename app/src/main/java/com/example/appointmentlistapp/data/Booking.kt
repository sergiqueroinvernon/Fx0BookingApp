package com.example.appointmentlistapp.data

import androidx.room.Entity
import com.example.appointmentlistapp.Driver

@Entity(tableName = "bookings")

data class Booking(
    // Booking section
    val bookingId: String,
    val status: String,
    val driver: String,
    val bookingDate: String,
    val description: String,

    // Trip details section
    val pickupDate: String,
    val handOverDate: String,
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
    val cancellationDate: String,
    val cancellationReason: String,

    // Notes section (nullable)
    val note: String,
    var isChecked: Boolean,
    val pickupTime: String,
    val internNumber: String,
    val processNumber: String,

    val id: String,
    val driverId: String,
    val appointmentDateTime: String,

    val vehicleRegistration: String? = null, // Used as 'vehicle' in Booking

    val purposeOfTripId: Int? = null,


    // Location and Odometer

    val odometerPickup: String? = null, // Used as 'odometerReadingPickup' in Booking
    val odometerReturn: String? = null, // Used as 'odometerReadingReturn' in Booking


    // Core flags and timestamps
    var createdAt: String,
    var updatedAt: String,


    // --- ADDED MISSING PROPERTIES ---

    // IDs (Mapping C# GUID/INT to Kotlin)
    val vehicleId: Int? = null,
    val statusId: String? = null, // C# uses 'string?' here, keeping it String?
    val vehiclePoolId: String? = null, // C# uses 'string?' here, keeping it String?

    val driverName: String? = null, // Already covered by 'driver' model/field
    val appointmentStatus: String? = null, // Covered by 'status'
    val tripPurposeName: String? = null, // Covered by 'purposeOfTrip'
    val vehiclePoolName: String? = null, // Covered by 'vehiclePool'
    val vehicleRegistrationName: String? = null // Covered by 'vehicleRegistration'



)

