package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appointmentlistapp.data.Booking


@Composable
fun BookingList(
    bookings: List<Booking>,
    onBookingSelected: (Booking) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp))
        {
            items(bookings) { booking ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable() { onBookingSelected(booking)}
                ){
                    Column(modifier = Modifier.padding(16.dp)){
                        Text(text = booking.vehicle, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Zweck: ${booking.purpose}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Datum: ${booking.date}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
}