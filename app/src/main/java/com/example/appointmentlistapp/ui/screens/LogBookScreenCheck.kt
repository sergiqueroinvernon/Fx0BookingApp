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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.components.LogbookCheckDetailsView
import com.example.appointmentlistapp.ui.components.LogbookCheckList
import com.example.appointmentlistapp.ui.viewmodel.LogBookCheckViewModel




@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogbookScreenCheck(viewModel: LogBookCheckViewModel = viewModel()) {

    var showDetails by remember { mutableStateOf(true) }
    var criteriaFilter by remember { mutableStateOf(true) }

    // 1. State collection from the ViewModel, using the correct variable names.
    val entries by viewModel.logbookEntries.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val checkedEntryIds by viewModel.checkedEntryIds.collectAsState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Prüfung der Fahrtenbücher meines Teams", modifier = Modifier.padding(10.dp), fontSize = 25.sp)
        }
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
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.filter),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text(if (criteriaFilter) "Filterkriterien" else "Filterkriterien")

            }
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.layout),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text(if (showDetails) "Layout" else "Layout")

            }
        }
        Row(modifier =  Modifier.fillMaxWidth()){


            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Hinzufügen")

            }
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.edit),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Bearbeiten")

            }
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.cancel),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Stornieren")

            }
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                   painter = painterResource(id = R.drawable.copy),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Kopieren")

            }
        }
        Row(modifier =  Modifier.fillMaxWidth()){


            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.finish),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Beenden")

            }
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.confirm),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Bestätigen")

            }
        }
        Row(modifier =  Modifier.fillMaxWidth()){
            Button(onClick = { showDetails = !showDetails}) {
                Icon(
                    painter = painterResource(id = R.drawable.approve),
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp).padding(end = 4.dp)
                )
                // Change button text based on the state
                Text("Genehmigen")
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

        }}

    }

}


