package com.example.appointmentlistapp.ui.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppointmentViewModel) {

    val tabs = listOf("Start", "Meine Buchungen", "Mein Fahrtenbuch", "Fahrtenbuchprüfung")
    val screenIds = listOf("start_screen", "booking_screen", "logbook_screen", "reports_screen")
    var selectedTabIndex by remember { mutableIntStateOf(0) } // Start ist Tab 0

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text("Fuhrparkmanagement") },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White
                    )
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            icon = {
                                if (title == "Start") {
                                    Icon(
                                        painter = painterResource(id = R.drawable.start), // Stellen Sie sicher, dass Sie diese Ressource haben
                                        contentDescription = "Start Tab Icon",
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> AppointmentListScreen(viewModel = viewModel)
                1 -> BookingScreen()
                2 -> LogbookScreen()
                3 -> Text("Fahrtenbuchprüfung", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}



fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: Date())
    } catch (e: Exception) {
        dateString
    }
}