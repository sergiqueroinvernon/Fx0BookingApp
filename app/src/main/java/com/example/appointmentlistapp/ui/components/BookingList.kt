// In ui/components/BookingList.kt
package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// THIS IS THE CRUCIAL IMPORT
import com.example.appointmentlistapp.data.Booking

@Composable
fun BookingList(
    bookings: List<Booking>,
    onBookingSelected: (Booking) -> Unit,
    onBookingCheckedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        item {
            HeaderRow()
            Divider()
        }
        items(bookings, key = { it.bookingId }) { booking ->
            DataRow(
                booking = booking,
                onRowClick = { onBookingSelected(booking) },
                onCheckedChange = { onBookingCheckedChange(booking.bookingId) }
            )
            Divider()
        }
    }
}

// ... (HeaderRow and DataRow composables remain the same) ...
@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 25.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.5f))
        Text("Status", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Vorgangsnr.", Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Fahrer", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Übergabe", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Fahrzeugpool", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Fahrzeug", Modifier.weight(1f), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DataRow(
    booking: Booking,
    onRowClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = booking.isChecked,
            onCheckedChange = { _ -> onCheckedChange() },
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = booking.status,
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp
        )
        Text(
            text = booking.bookingId,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Text(
            text = booking.driver,
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp
        )
        Column(
            modifier = Modifier.weight(1.5f)
        ) {
            booking.pickupDate?.let {
                Text(
                    text = it,
                    fontSize = 14.sp
                )
            }
            Text(
                text = booking.returnDate,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Text(
            text = booking.vehiclePool,
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp
        )
        Text(
            text = booking.vehicle,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp)
    }
}