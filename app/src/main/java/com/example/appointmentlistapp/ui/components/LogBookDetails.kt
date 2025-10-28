package com.example.appointmentlistapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
fun LogbookDetailView(logbook: Logbook?, modifier: Modifier = Modifier) {
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
                        label1 = "Eintragsnr.",
                        value1 = logbook.entryId.toString(), // Fix: Convert Long to String
                        label2 = "Status",
                        value2 = logbook.status // Fix: Corrected property access
                    )
                    // Fahrer (Placeholder as property is missing in Logbook data class)
                    DetailRow(
                        label1 = "Fahrer",
                        value1 = "Fahrername"
                    )
                }

                // Section: Angaben zur Fahrt
                item {
                    SectionHeader("Angaben zur Fahrt")
                    DetailRow(
                        label1 = "Datum Start", value1 = logbook.startTime.format(dateFormatter),
                        label2 = "Uhrzeit Start", value2 = logbook.startTime.format(timeFormatter)
                    )
                    DetailRow(
                        label1 = "Datum Ziel", value1 = logbook.endTime.format(dateFormatter),
                        label2 = "Uhrzeit Ziel", value2 = logbook.endTime.format(timeFormatter)
                    )
                    DetailRow(
                        label1 = "Fahrzeug", value1 = logbook.vehicle?.registration,
                     //   label2 = "Interne Nr.", value2 = logbook.internalNumber // Placeholder
                    )
                    DetailRow(
                        label1 = "Reisezweck",
                        value1 = logbook.purposeOfTrip // Fix: Corrected property name
                    )

                    // Fields from Screenshot: Reaktionszeitfahrt / Begründung
                    DetailRow(
                        label1 = "Reaktionszeitfahrt",
                        value1 = if (logbook.isResponseTimeTrip) "Ja" else "Nein",
                        label2 = "Begründung",
                        value2 = logbook.justification
                    )

                    DetailRow(label1 = "Ort Fahrtbeginn", value1 = logbook.startLocation)

                    //fahrtstrecke / ziel (assuming it's end location of the last leg)
                   // DetailRow(label1 = "Fahrtstrecke / Ziel", value1 = logbook.tripLegs.lastOrNull()?.endLocation)

                    DetailRow(
                        label1 = "km-Stand Start", value1 = "${logbook.startOdometer} km",
                        label2 = "km-Stand Ziel", value2 = "${logbook.endOdometer} km"
                    )
                    DetailRow(label1 = "Strecke", value1 = "${logbook.distance} km")
                }

                /*
                // Section: Teilstrecken
                if (logbook.tripLegs.isNotEmpty()) {
                    item { SectionHeader("Teilstrecken") }
                    itemsIndexed(logbook.tripLegs) { index, leg ->
                        Text(
                            text = "${index + 1}.",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        DetailRow(
                            label1 = "Uhrzeit Start", value1 = leg.startTime.format(timeFormatter),
                            label2 = "Ort Start", value2 = leg.startLocation
                        )
                        DetailRow(
                            label1 = "Uhrzeit Ziel", value1 = leg.endTime.format(timeFormatter),
                            label2 = "Ort Ziel", value2 = leg.endLocation
                        )
                        // Reaktionszeitfahrt / Begründung for leg (missing in data class, adding placeholder)
                        DetailRow(
                            label1 = "Reaktionszeitfahrt", value1 = "-",
                            label2 = "Begründung", value2 = "-"
                        )

                        if(index < logbook.tripLegs.size -1) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
*/



//                // Section: Genehmigung
//                logbook.approval?.let {
//                    item {
//                        SectionHeader("Genehmigung")
//                        DetailRow(label1 = "Genehmigt am", value1 = it.approvedOn?.format(dateFormatter))
//                        DetailRow(label1 = "Genehmigt von", value1 = it.approvedBy)
//                        // Changed label to match screenshot
//                        DetailRow(label1 = "Anmerkung Genehmigung", value1 = it.notes)
//                    }
//                }
//
//                // Section: Storno (Cancellation)
//                logbook.cancellation?.let {
//                    item {
//                        SectionHeader("Storno")
//                        DetailRow(label1 = "Storniert am", value1 = it.cancelledOn?.format(dateFormatter))
//                        DetailRow(label1 = "Storniert von", value1 = it.cancelledBy)
//                        DetailRow(label1 = "Stornogrund", value1 = it.reason)
//                    }
//                }



                // Section: Anmerkung
                item {
                    SectionHeader("Anmerkung")
                    // Changed label to match screenshot
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
    // Check if at least one value is non-null/non-empty before rendering the row
    if (!value1.isNullOrBlank() || !value2.isNullOrBlank()) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            InfoColumn(label = label1, value = value1, modifier = Modifier.weight(1f))
            if (label2 != null) {
                InfoColumn(label = label2, value = value2, modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.weight(1f)) // Maintain column alignment
            }
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