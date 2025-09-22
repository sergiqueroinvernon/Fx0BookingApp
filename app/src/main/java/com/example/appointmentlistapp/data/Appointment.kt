package com.example.appointmentlistapp.data.model

import com.example.appointmentlistapp.Driver

 data class Appointment(
    val id: String,
    val driverId: String,
    val driver: Driver?,
    val appointmentDateTime: String,
    val status: String,
    val description: String,
    var isChecked: Boolean = false,
    var createdAt: String,
    var updatedAt: String

)