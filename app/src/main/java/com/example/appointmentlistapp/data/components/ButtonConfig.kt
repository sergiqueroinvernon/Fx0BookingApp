package com.example.appointmentlistapp.data.components

import androidx.room.Entity

@Entity(tableName = "button_configs", primaryKeys=["id", "clientId", "screenId"])
data class ButtonConfig(
    val id: Int,
    val clientId: String,
    val screenId: String,
    val buttonName: String?,
    val action: String?,
    val type: String,
    val isVisible: Int,
    val text: String,
    val IconData: ByteArray?
)
