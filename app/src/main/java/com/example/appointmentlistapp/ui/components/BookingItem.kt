package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appointmentlistapp.data.Booking // Import Booking for better consistency, assuming Appointment is an alias for Booking
import com.example.appointmentlistapp.ui.screens.formatDate


@Composable
fun BookingItem(
    booking: Booking,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    isSelected: Boolean // Used to determine visual state
) {
    val isPending = booking.status.equals("Pending", ignoreCase = true)
    val isCompleted = booking.status.equals("Completed", ignoreCase = true)

    // 1. Use CardColors to change background when selected
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        // 2. Wrap Checkbox and details in a Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Add padding here for the whole item
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- Checkbox Column ---
            Column(
                modifier = Modifier
                    .width(48.dp) // Fixed width for checkbox area
                    .padding(start = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isPending) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange,
                    )
                }
            }

            // --- Details Column (Clickable) ---
            Column(
                // 3. Apply the onClick and fillMaxWidth to the details column
                modifier = Modifier
                    .weight(1f) // Take up remaining space
                    .clickable(onClick = onClick) // THIS IS THE KEY TO SELECTION
                    .padding(end = 16.dp) // Padding on the right
            ) {
                Text(
                    text = booking.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Vorgangsnrs.: ${booking.bookingId?: "N/A"}")
                Text(text = "Statuss.: ${booking.status?: "N/A"}")
                Text(text = "Reserviert von.: ${booking.bookingId?: "N/A"}")
                Text(text = "Fahrer: ${booking.driver?: "N/A"}")

                Text(text = "Übergabe ${booking.handOverDate?: "N/A"}")
                Text(text = "Rücknahme: ${booking.returnDate ?: "N/A"}")

                Text(text = "Fahrzeugpool ${booking.vehiclePool ?: "N/A"}")
                Text(text = "Reisezweck: ${booking.purposeOfTrip ?: "N/A"}")

                Text(text = "Fahrzeugpool ${booking.driver ?: "N/A"}")
                Text(text = "Interne Nr.: ${booking.internNumber ?: "N/A"}")

                // Reduced height for tighter packing
                Text(text = "Date: ${formatDate(booking.bookingDate)}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Status: ")
                    Text(
                        text = booking.status,
                        color = if (isCompleted) Color(0xFF388E3C) else Color(0xFFFBC02D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}