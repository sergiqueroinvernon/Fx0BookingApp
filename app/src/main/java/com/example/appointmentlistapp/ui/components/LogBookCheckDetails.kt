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
import com.example.appointmentlistapp.data.LogbookEntry
import com.example.appointmentlistapp.data.LogbookStatus
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookCheckDetailsView(logbook: LogbookEntry?, modifier: Modifier = Modifier) {
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
                // Section: Eintrag
                item {
                    SectionHeader("Eintrag")
                    DetailRow(
                        label1 = "Eintragsnr.", value1 = logbook.id.toString(),
                        label2 = "Status", value2 = when (logbook.status) {
                            LogbookStatus.CONFIRMED -> "Bestätigt"
                            LogbookStatus.NOT_CONFIRMED -> "Nicht bestätigt"
                        }
                    )
                }

                // Section: Angaben zur Fahrt
                item {
                    SectionHeader("Angaben zur Fahrt")
                    DetailRow(
                        label1 = "Startdatum", value1 = logbook.startTime.format(dateFormatter),
                        label2 = "Startzeit", value2 = logbook.startTime.format(timeFormatter)
                    )
                    DetailRow(
                        label1 = "Zieldatum", value1 = logbook.endTime.format(dateFormatter),
                        label2 = "Zielzeit", value2 = logbook.endTime.format(timeFormatter)
                    )
                    DetailRow(
                        label1 = "Fahrzeug", value1 = logbook.vehicle.licensePlate,
                        label2 = "Interne Nr.", value2 = logbook.vehicle.internNumber
                    )
                    DetailRow(label1 = "Reisezweck", value1 = logbook.purpose)
                    DetailRow(label1 = "Startort", value1 = logbook.startLocation)
                    DetailRow(
                        label1 = "km-Stand Start", value1 = "${logbook.startOdometer} km",
                        label2 = "km-Stand Ziel", value2 = "${logbook.endOdometer} km"
                    )
                    DetailRow(label1 = "Strecke", value1 = "${logbook.distance} km")
                }

                // Section: Teilstrecken
                if (logbook.tripLegs.isNotEmpty()) {
                    item { SectionHeader("Teilstrecken") }
                    items(logbook.tripLegs.size) { index ->
                        val leg = logbook.tripLegs[index]
                        DetailRow(
                            label1 = "Startort ${index + 1}", value1 = leg.startLocation,
                            label2 = "Startzeit ${index + 1}", value2 = leg.startTime.format(timeFormatter)
                        )
                        DetailRow(
                            label1 = "Zielort ${index + 1}", value1 = leg.endLocation,
                            label2 = "Zielzeit ${index + 1}", value2 = leg.endTime.format(timeFormatter)
                        )
                        if(index < logbook.tripLegs.size -1) Spacer(modifier = Modifier.height(8.dp))
                    }
                }


                // Section: Genehmigung
                logbook.approval?.let {
                    item {
                        SectionHeader("Genehmigung")
                        DetailRow(label1 = "Genehmigt am", value1 = it.approvedOn?.format(dateFormatter))
                        DetailRow(label1 = "Genehmigt von", value1 = it.approvedBy)
                        DetailRow(label1 = "Anmerkung Genehmigung", value1 = it.notes)
                    }
                }

                // Section: Anmerkung
                item {
                    SectionHeader("Anmerkung")
                    DetailRow(label1 = "Anmerkung", value1 = logbook.notes)
                }
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

