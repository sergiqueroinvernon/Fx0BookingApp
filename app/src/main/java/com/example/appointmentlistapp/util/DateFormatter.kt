package com.example.appointmentlistapp.util


import java.text.SimpleDateFormat
import java.util.*

public fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: Date())
    } catch (e: Exception) {
        dateString
    }
}