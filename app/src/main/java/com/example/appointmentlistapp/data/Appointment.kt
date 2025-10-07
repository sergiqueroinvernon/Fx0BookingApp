package com.example.appointmentlistapp.data.model

import com.example.appointmentlistapp.Driver
import com.example.appointmentlistapp.data.PurposeOfTrip

data class Appointment(
   val id: String,
   val driverId: String,
   val driver: Driver?, // Reference to the Driver model
   val appointmentDateTime: String,
   val status: String,
   val description: String,
   val vehicleRegistration: String? = null, // Used as 'vehicle' in Booking
   val vehiclePool: String? = null,
   val purposeOfTrip: String? = null,
   val purposeOfTripId: Int? = null,
   val bookingDate: String? = null,
   val pickupTime: String? = null,
   val returnDate: String? = null,
   val returnTime: String? = null,

    // Location and Odometer
   val pickupLocation: String? = null,
   val returnLocation: String? = null,
   val odometerPickup: String? = null, // Used as 'odometerReadingPickup' in Booking
   val odometerReturn: String? = null, // Used as 'odometerReadingReturn' in Booking
   val distance: String? = null,
   val handOverDate: String? = null,
   val internNumber: String? = null,


    // Administrative/Cancellation
   val cancellationDate: String? = null,
   val cancellationReason: String? = null,
   val note: String? = null,

    // Core flags and timestamps
   var isChecked: Boolean = false,
   var createdAt: String,
   var updatedAt: String,

   val processNumber: String? = null,

   )