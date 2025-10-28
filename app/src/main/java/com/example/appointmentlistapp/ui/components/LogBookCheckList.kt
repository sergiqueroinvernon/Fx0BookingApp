package com.example.appointmentlistapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
fun LogbookCheckList(
    entries: List<Logbook>,
    selectedEntry: Logbook?,
    checkedEntryIds: Set<Long>,
    onEntrySelected: (Logbook) -> Unit,
    onEntryCheckedChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        item {
            HeaderRow()
            Divider()
        }
        items(entries, key = { it.entryId }) { entry ->
            val isSelected = entry.entryId == selectedEntry?.entryId
            val isChecked = checkedEntryIds.contains(entry.entryId)
            DataRow(
                entry = entry,
                isSelected = isSelected,
                isChecked = isChecked,
                onRowClick = { onEntrySelected(entry) },
                onCheckedChange = { onEntryCheckedChange(entry.entryId) }
            )
            Divider()
        }
    }
}

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
        Text("Eintragsnr.", Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text("Start", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Fahrzeug", Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
        Text("Zweck", Modifier.weight(1f), fontWeight = FontWeight.Bold)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DataRow(
    entry: Logbook,
    isSelected: Boolean,
    isChecked: Boolean,
    onRowClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onRowClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { _ -> onCheckedChange() },
            modifier = Modifier.weight(0.5f)
        )
        /*
        Text(
            text = {""},
            modifier = Modifier.weight(1.5f),
            fontSize = 14.sp,
            //color = if (entry.status == LogbookStatus.CONFIRMED) Color(0xFF006400) else Color.Gray,
            fontWeight = FontWeight.Bold
        )
        */

       // Text(entry.id.toString(), Modifier.weight(1f), fontSize = 14.sp)
        Text(entry.startTime.format(dateFormatter), Modifier.weight(1.5f), fontSize = 14.sp)
        //Text(entry.purpose ?: "-", Modifier.weight(1f), fontSize = 14.sp)
        //  Text(entry.vehicle?. ?: , Modifier.weight(1.5f), fontSize = 14.sp)
    }
}

