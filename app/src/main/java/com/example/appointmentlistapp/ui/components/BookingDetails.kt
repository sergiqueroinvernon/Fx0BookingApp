package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appointmentlistapp.data.Booking

@Composable
fun BookingDetails(booking: Booking?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (booking != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Buchungsdetails", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("ID: ${booking.id}", style = MaterialTheme.typography.bodyLarge)
                Text("Fahrzeug: ${booking.vehicle}", style = MaterialTheme.typography.bodyLarge)
                Text("Datum: ${booking.date}", style = MaterialTheme.typography.bodyLarge)
                Text("Reisezweck: ${booking.purpose}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Details: ${booking.details}", style = MaterialTheme.typography.bodyMedium)
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Buchungsdetails", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("ID: ${booking.id}", style = MaterialTheme.typography.bodyLarge)
                Text("Fahrzeug: ${booking.vehicle}", style = MaterialTheme.typography.bodyLarge)
                Text("Datum: ${booking.date}", style = MaterialTheme.typography.bodyLarge)
                Text("Reisezweck: ${booking.purpose}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Details: ${booking.details}", style = MaterialTheme.typography.bodyMedium)
            }



        } else {
            Text("Bitte eine Buchung aus der Liste auswählen.")
        }
    }
}