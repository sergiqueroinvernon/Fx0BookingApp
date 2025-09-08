package com.example.appointmentlistapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.ui.components.LogbookDetailView
import com.example.appointmentlistapp.ui.components.LogbookList
import com.example.appointmentlistapp.ui.viewmodel.LogBookViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookScreen(viewModel: LogBookViewModel = viewModel()) {
    // 1. State collection from the ViewModel, using the correct variable names.
    val entries by viewModel.logbookEntries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val checkedEntryIds by viewModel.checkedEntryIds.collectAsState()

    // The Row composable arranges the list and detail view side-by-side.
    Row(Modifier.fillMaxSize()) {

        // 2. Master Pane (List) - Calling the correct component with the correct parameters.
        LogbookList(
            entries = entries,
            selectedEntry = selectedEntry,
            checkedEntryIds = checkedEntryIds,
            onEntrySelected = { entry ->
                // Calling the correct ViewModel function to select an entry.
                viewModel.selectEntry(entry)
            },
            onEntryCheckedChange = { entryId ->
                viewModel.toggleEntryChecked(entryId)
            },
            modifier = Modifier.weight(2f) // Give more space to the list
        )

        VerticalDivider()

        // 3. Detail Pane - Calling the correct component with the correct parameter name.
        LogbookDetailView(
            logbook = selectedEntry,
            modifier = Modifier.weight(1f) // Give less space to details
        )
    }
}

