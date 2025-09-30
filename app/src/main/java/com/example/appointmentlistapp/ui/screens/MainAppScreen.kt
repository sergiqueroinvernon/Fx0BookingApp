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
import androidx.lifecycle.viewmodel.compose.viewModel // Needed to call viewModel() inside LogbookScreen
import com.example.appointmentlistapp.R
import com.example.appointmentlistapp.ui.viewmodel.AppointmentViewModel
import com.example.appointmentlistapp.ui.viewmodel.LogBookViewModel // Assuming this exists for LogbookScreen
import com.example.appointmentlistapp.viewmodels.BookingViewModel // Assuming this exists for BookingScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppointmentViewModel) {
    // NOTE: This ViewModel (AppointmentViewModel) handles QR scan state and appointment fetching (Tab 0).
    // The other tabs need their respective ViewModels.

    val tabs = listOf("Start", "Meine Buchungen", "Mein Fahrtenbuch", "Fahrtenbuchprüfung")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // --- Instantiating ViewModels for other tabs ---

    // We assume BookingScreen uses BookingViewModel:
    val bookingViewModel: BookingViewModel = viewModel()

    // We assume LogbookScreen uses LogBookViewModel:
    val logbookViewModel: LogBookViewModel = viewModel()


    // REMOVED: val bookingRepository = remember { com.example.appointmentlistapp.data.BookingRepository }
    // ^ Instantiating Repositories here violates composition principles.

    // The current scannedDriverId is the shared state.
    val scannedDriverId by viewModel.scannedDriverId.collectAsState()


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
                                        painter = painterResource(id = R.drawable.start),
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
            // --- SCREEN ROUTING ---
            when (selectedTabIndex) {
                // 0: Start Screen (Uses the main AppointmentViewModel)
                0 -> AppointmentListScreen(viewModel = viewModel)

                // 1: Booking Screen (Uses its dedicated ViewModel, but needs the scanned ID)
                1 -> BookingScreen(
                    viewModel = bookingViewModel,
                    scannedDriverId = scannedDriverId,
                )

                // 2: Logbook Screen (Uses its dedicated ViewModel, passing the shared ID)
                2 -> LogbookScreen(
                    viewModel = logbookViewModel,
                    scannedDriverId = scannedDriverId,
                )

                // 3: Reports Screen
                3 -> Text("Fahrtenbuchprüfung", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// NOTE: formatDate function remains outside the composable, where it should be.
fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        formatter.format(parser.parse(dateString) ?: Date())
    } catch (e: Exception) {
        dateString
    }
}