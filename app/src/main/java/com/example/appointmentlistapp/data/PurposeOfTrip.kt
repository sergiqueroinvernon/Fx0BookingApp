package com.example.appointmentlistapp.data

import androidx.room.Entity

@Entity(tableName = "PurposeOfTrips")
data class PurposeOfTrip (
    val id: Int,
    val purpose: String
)