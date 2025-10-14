package com.example.appointmentlistapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.appointmentlistapp.ui.camera.CameraPreview
import com.example.appointmentlistapp.ui.components.AppointmentItem
import com.example.appointmentlistapp.ui.viewmodel.AppointmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(viewModel: AppointmentViewModel) {
    val appointments by viewModel.appointments.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scannedDriverId by viewModel.scannedDriverId.collectAsState()
    val context = LocalContext.current
    var showQrScanner by remember { mutableStateOf(false) }

    val selectedAppointmentCount by remember(appointments) {
        derivedStateOf { appointments.count { it.isChecked && it.status.equals("Pending", ignoreCase = true) } }
    }

    val allAppointmentsChecked by remember(appointments) {
        derivedStateOf {
            val pendingAppointments = appointments.filter { it.status.equals("Pending", ignoreCase = true) }
            pendingAppointments.isNotEmpty() && pendingAppointments.all { it.isChecked }
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showQrScanner = true
        } else {
            Toast.makeText(context, "Kamera-Berechtigung verweigert. QR-Code kann nicht gescannt werden.", Toast.LENGTH_LONG).show()
            viewModel.setErrorMessage("Kamera-Berechtigung verweigert. QR-Code kann nicht gescannt werden.")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showQrScanner) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onBarcodeScanned = { barcodeValue ->
                    if (barcodeValue != null) {
                        viewModel.setScannedDriverId(barcodeValue)
                        showQrScanner = false
                    } else {
                        Toast.makeText(context, "QR-Code nicht erkannt oder ungültig.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { viewModel.fetchAppointments() }) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh")
                        }
                        if (scannedDriverId != null && !showQrScanner) {
                            Button(
                                onClick = { viewModel.checkInSelectedAppointments() },
                                enabled = selectedAppointmentCount > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Text("Check In ($selectedAppointmentCount)")
                            }
                        }
                        IconButton(onClick = {
                            when {
                                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                                    showQrScanner = true
                                }
                                else -> {
                                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = "Scan QR Code")
                        }
                    }
                )

                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Ein unbekannter Fehler ist aufgetreten.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        )
                    }
                    scannedDriverId == null -> {
                        // Welcome Text...
                        Text(
                            text = "Bitte scannen Sie einen QR-Code, um Termine anzuzeigen.",
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(100.dp)
                        )
                    }
                    appointments.isEmpty() -> {
                        Text(
                            text = "Keine Termine für Fahrer-ID ${scannedDriverId ?: "N/A"} gefunden.",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = allAppointmentsChecked,
                                        onCheckedChange = { isChecked -> viewModel.toggleSelectAll(isChecked) },
                                        enabled = appointments.any { it.status.equals("Pending", ignoreCase = true) }
                                    )
                                    Text(text = "Alle auswählen", fontWeight = FontWeight.Bold)
                                }
                                Divider()
                            }
                            items(
                                appointments,
                                key = { appointment -> appointment.id }
                            ) { appointment ->
                                AppointmentItem(
                                    appointment,
                                    isChecked = appointment.isChecked
                                ) { viewModel.toggleAppointmentChecked(appointment.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}