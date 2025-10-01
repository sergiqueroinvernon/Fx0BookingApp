package com.example.appointmentlistapp.data

import androidx.room.Entity
import com.example.appointmentlistapp.data.model.Appointment
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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
    val pickupTime: String
)

