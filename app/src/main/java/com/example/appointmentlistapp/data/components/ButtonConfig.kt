package com.example.appointmentlistapp.data.components

import androidx.room.Entity

@Entity(tableName = "button_configs", primaryKeys=["id", "clientId", "screenId"])
data class ButtonConfig(
    val id: String,
    val text: String,
    val isVisible: Boolean,
    val clientId: String,
    val screenId: String,
    val type: String
)
