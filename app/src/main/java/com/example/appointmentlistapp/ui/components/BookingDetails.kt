package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appointmentlistapp.data.Booking // Your new data class

@Composable
fun BookingDetails(booking: Booking?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (booking != null) {
            LazyColumn(  
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Buchung
                item {
                    SectionHeader("Buchung")
                    DetailRow(
                        label1 = "Vorgangsnr.", value1 = booking.bookingId,
                        label2 = "Status", value2 = booking.status
                    )
                    //DetailRow(label1 = "Fahrer", value1 = booking.driver)
                }

                // Section: Angaben zur Fahrt
                item {
                    SectionHeader("Angaben zur Fahrt")
                    DetailRow(
                        label1 = "Übergabedatum", value1 = booking.pickupDate,
                        label2 = "Übergabezeit", value2 = booking.pickupTime
                    )
                    DetailRow(
                        label1 = "Rücknahmedatum", value1 = booking.returnDate,
                        label2 = "Rücknahmezeit", value2 = booking.returnTime
                    )
                    DetailRow(
                        label1 = "Fahrzeug", value1 = booking.vehicle,
                        label2 = "Fahrzeugpool", value2 = booking.vehiclePool
                    )
                    DetailRow(
                        label1 = "Reisezweck", value1 = booking.purposeOfTrip,
                        label2 = "Übergabeort", value2 = booking.pickupLocation
                    )
                    DetailRow(
                        label1 = "Rücknahmeort Adresse",
                        value1 = booking.returnLocation
                    )

                    DetailRow(
                        label1 = "km-Stand Übergabe", value1 = booking.odometerReadingPickup,
                        label2 = "km-Stand Rücknahme", value2 = booking.odometerReadingReturn
                    )
                    DetailRow(label1 = "Strecke", value1 = booking.distance)
                }

                // Section: Storno
                item {
                    SectionHeader("Storno")
                    DetailRow(label1 = "Stornodatum", value1 = booking.cancellationDate)
                    DetailRow(label1 = "Stornogrund", value1 = booking.cancellationReason)
                }

                // Section: Anmerkung
                item {
                    SectionHeader("Anmerkung")
                    DetailRow(label1 = "Anmerkung", value1 = booking.note)
                }
            }
        } else {
            Text("Bitte eine Buchung aus der Liste auswählen.")
        }
    }
}

// Helper composable to create a section header
@Composable
private fun SectionHeader(title: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Divider()
    }
}

// Helper composable to create a row with label and value pairs
@Composable
private fun DetailRow(
    label1: String, value1: String?,
    label2: String? = null, value2: String? = null
) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        InfoColumn(label = label1, value = value1, modifier = Modifier.weight(1f))
        if (label2 != null) {
            InfoColumn(label = label2, value = value2, modifier = Modifier.weight(1f))
        }
    }
}

// Helper for a single label-value column
@Composable
private fun InfoColumn(label: String, value: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = value ?: "-", // Show "-" if value is null or empty
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}