package com.example.appointmentlistapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.appointmentlistapp.data.Logbook
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookCheckDetailsView(logbook: Logbook?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (logbook != null) {
            val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            }
        } else {
            Text("Bitte einen Eintrag aus der Liste auswählen.")
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
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value ?: "-", // Show "-" if value is null or empty
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
    }
}

