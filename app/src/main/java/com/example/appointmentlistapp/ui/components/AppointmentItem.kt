package com.example.appointmentlistapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.ui.screens.formatDate


@Composable
fun AppointmentItem(
    appointment: Appointment,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val isPending = appointment.status.equals("Pending", ignoreCase = true)
    val isCompleted = appointment.status.equals("Completed", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isPending) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Platzhalter für Konsistenz
            }
            Column {
                Text(
                    text = appointment.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Driver: ${appointment.driver?.name ?: "N/A"}")
                Text(text = "Date: ${formatDate(appointment.appointmentDateTime)}")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Status: ")
                    Text(
                        text = appointment.status,
                        color = if (isCompleted) Color(0xFF388E3C) else Color(0xFFFBC02D),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}