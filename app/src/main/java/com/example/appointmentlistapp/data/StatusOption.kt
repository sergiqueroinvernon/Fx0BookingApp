package com.example.appointmentlistapp.data

import androidx.room.Entity

@Entity(tableName = "StatusOptions")
data class StatusOption(
    val id: Int,
    val option: String
)