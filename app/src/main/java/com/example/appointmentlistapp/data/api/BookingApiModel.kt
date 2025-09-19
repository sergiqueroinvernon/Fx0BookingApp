package com.example.appointmentlistapp.data.api

import com.example.appointmentlistapp.Driver

class BookingApiModel (
    var id: String,
    var driverId: String,
    var driver: Driver,
    var appointmentDateTime: String,
    var status: String,
    var description: String,
    var createdAt: String,
    var updatedAt: String


    )