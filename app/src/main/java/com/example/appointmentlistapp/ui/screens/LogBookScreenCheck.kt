package com.example.appointmentlistapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.components.LogbookCheckDetailsView
import com.example.appointmentlistapp.ui.components.LogbookCheckList
import com.example.appointmentlistapp.ui.viewmodel.LogBookCheckViewModel

// --- Helper Composable for Buttons (Optional but cleaner) ---
@Composable
fun LogbookActionButton(
    iconResId: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier
                .size(24.dp)
                .padding(end = 4.dp)
        )
        Text(text)
    }
}
// ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookScreenCheck(viewModel: LogBookCheckViewModel = viewModel()) {

    var showDetails by remember { mutableStateOf(true) }
    var criteriaFilter by remember { mutableStateOf(false) } // Changed to false to show difference

    // State collection from the ViewModel
   //al entries by viewModel.logbookEntries.collectAsState()
    //val selectedEntry by viewModel.selectedEntry.collectAsState()
    val checkedEntryIds by viewModel.checkedEntryIds.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Title Header for the whole screen
        Text(
            text = "Prüfung der Fahrtenbücher meines Teams",
            modifier = Modifier.padding(10.dp),
            fontSize = 25.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Master/Detail Layout Start
        Row(Modifier.fillMaxSize()) {

            // Master Pane Column (List + Buttons)
            Column(modifier = Modifier.weight(2f)) {

                // --- Top Row of Buttons (View Toggles) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Toggle Details Button
                    LogbookActionButton(
                        iconResId = R.drawable.info,
                        text = if (showDetails) "Details ausblenden" else "Details anzeigen",
                        onClick = { showDetails = !showDetails }
                    )

                    // Toggle Filter Button
                    LogbookActionButton(
                        iconResId = R.drawable.filter,
                        text = "Filterkriterien",
                        onClick = { criteriaFilter = !criteriaFilter } // Actual action for filter
                    )

                    // Layout Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.layout,
                        text = "Layout",
                        onClick = { /* TODO: Implement layout change logic */ }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Middle Row of Buttons (Edit Actions) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Hinzufügen (Add) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.add,
                        text = "Hinzufügen",
                        onClick = { /* TODO: Implement Add Entry logic */ }
                    )

                    // Bearbeiten (Edit) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.edit,
                        text = "Bearbeiten",
                        onClick = { /* TODO: Implement Edit Entry logic on selectedEntry */ }
                    )

                    // Stornieren (Cancel) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.cancel,
                        text = "Stornieren",
                        onClick = { /* TODO: Implement Cancel Entry logic on selectedEntry */ }
                    )

                    // Kopieren (Copy) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.copy,
                        text = "Kopieren",
                        onClick = { /* TODO: Implement Copy Entry logic on selectedEntry */ }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Bottom Row of Buttons (Finalization Actions) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Beenden (Finish) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.finish,
                        text = "Beenden",
                        onClick = { /* TODO: Implement Finish Check process */ }
                    )

                    // Bestätigen (Confirm) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.confirm,
                        text = "Bestätigen",
                        onClick = { /* TODO: Implement Confirm selected entries logic */ }
                    )

                    // Genehmigen (Approve) Button (Placeholder action)
                    LogbookActionButton(
                        iconResId = R.drawable.approve,
                        text = "Genehmigen",
                        onClick = { /* TODO: Implement Approve selected entries logic */ }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
/*
                // Master Pane (List)
                LogbookCheckList(
                    entries = entries,
                   // selectedEntry = selectedEntry,
                    checkedEntryIds = checkedEntryIds,
                    onEntrySelected = { entry ->
                        viewModel.selectEntry(entry)
                    },
                    onEntryCheckedChange = { entryId ->
                        viewModel.toggleEntryChecked(entryId)
                    },
                    // Fill the remaining space in the column
                   // modifier = Modifier.fillMaxSize()
                )
                */

            } // END Master Pane Column

            VerticalDivider()

            /*
            // Detail Pane
            if (showDetails) {
                LogbookCheckDetailsView(
                    logbook = selectedEntry,
                    modifier = Modifier.weight(1f) // Details Pane is smaller
                )
            }
            */

        } // END Master/Detail Row
    }
}