package com.example.appointmentlistapp.data

import androidx.room.Entity

@Entity(tableName = "PurposeOfTrip")
data class PurposeOfTrip (
    val id: Int,
    val purpose: String
)