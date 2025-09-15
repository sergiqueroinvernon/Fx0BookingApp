package com.example.appointmentlistapp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.components.LogbookCheckDetailsView
import com.example.appointmentlistapp.ui.components.LogbookDetailView
import com.example.appointmentlistapp.ui.components.LogbookCheckList
import com.example.appointmentlistapp.ui.viewmodel.LogBookCheckViewModel




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookScreenCheck(viewModel: LogBookCheckViewModel = viewModel()) {

    var showDetails by remember { mutableStateOf(true) }


    // 1. State collection from the ViewModel, using the correct variable names.
    val entries by viewModel.logbookEntries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val checkedEntryIds by viewModel.checkedEntryIds.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier =  Modifier.fillMaxWidth()){


            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.info),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text(if (showDetails) "Hide Details" else "Show Details")

            }
        }

        // The Row composable arranges the list and detail view side-by-side.
        Row(Modifier.fillMaxSize()) {
            // 2. Master Pane (List) - Calling the correct component with the correct parameters.
            LogbookCheckList(
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

            if(showDetails)
            {
                LogbookCheckDetailsView(
                    logbook = selectedEntry,
                    modifier = Modifier.weight(1f) // Give less space to details
                )
            }
            // 3. Detail Pane - Calling the correct component with the correct parameter name.

        }

    }

}

