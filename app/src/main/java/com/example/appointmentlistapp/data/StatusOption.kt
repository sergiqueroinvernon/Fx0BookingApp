package com.example.appointmentlistapp.data

import androidx.room.Entity

@Entity(tableName = "StatusOption")
data class StatusOption(
    val id: Int,
    val option: String
)