package com.example.appointmentlistapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.appointmentlistapp.ui.theme.LoginScreen
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// =====================================================================================
// == BUILD.GRADLE.KTS DEPENDENCIES - (Instructions remain in comments for reference) ==
// =====================================================================================
// dependencies {
//      ...
//      // ViewModel for Compose (ensure you have version 2.8.3 from previous instructions)
//      implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
//
//      // Retrofit for networking
//      implementation("com.squareup.retrofit2:retrofit:2.9.0")
//      implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//
//      // CameraX dependencies (ensure consistent versions, e.g., 1.3.3)
//      implementation("androidx.camera:camera-core:1.3.3")
//      implementation("androidx.camera:camera-camera2:1.3.3")
//      implementation("androidx.camera:camera-lifecycle:1.3.3")
//      implementation("androidx.camera:camera-view:1.3.3")
//      implementation("androidx.camera:camera-extensions:1.3.3")
//
//      // ML Kit Barcode Scanning
//      implementation("com.google.mlkit:barcode-scanning:17.2.0")
// }
// =====================================================================================


// =====================================================================================
// == ANDROIDMANIFEST.XML - (Instructions remain in comments for reference) ==
// =====================================================================================
// <manifest ...>
//      <uses-permission android:name="android.permission.INTERNET" />
//      <uses-permission android:name="android.permission.CAMERA" />
//
//      <application
//          ...
//          android:usesCleartextTraffic="true">
//          ...
//      </application>
// </manifest>
// =====================================================================================


// =====================================================================================
// == DATA MODELS ==
// =====================================================================================
data class Appointment(
    val id: String,
    val driverId: String,
    val driver: Driver?,
    val appointmentDateTime: String,
    val status: String,
    val description: String,
    var isChecked: Boolean = false
)

data class Driver(
    val id: String,
    val name: String,
    val email: String
)

// =====================================================================================
// == CAMERA PREVIEW AND BARCODE ANALYZER ==
// =====================================================================================
typealias BarcodeAnalyserListener = (barcodeValue: String?) -> Unit

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: BarcodeAnalyserListener
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context)}

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val cameraExecutor = Executors.newSingleThreadExecutor()

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val barcodeScannerOptions = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()

                val barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions)

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(barcodeScanner) { barcodeValue ->
                            onBarcodeScanned(barcodeValue)
                        })
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}

private class BarcodeAnalyzer(
    private val barcodeScanner: BarcodeScanner,
    private val listener: BarcodeAnalyserListener
) : ImageAnalysis.Analyzer {

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let {
                        listener(it)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "Barcode scanning failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}


// =====================================================================================
// == NETWORKING LAYER (Retrofit) ==
// =====================================================================================
private const val BASE_URL = "http://172.26.140.23:5251/" // IMPORTANT: Change port if yours is different

interface ApiService {

    @GET(value="api/appointments/driver/{driverId}")
    suspend fun getAppointmentsByDriverId(@Path("driverId") id: String): List<Appointment>

    @POST("api/appointments/{id}/checkin")
    suspend fun checkInAppointment(@Path("id") id: String): Response<Unit>
}

object RetrofitInstance {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}


// =====================================================================================
// == VIEWMODEL (To hold state and handle logic) ==
// =====================================================================================
class AppointmentViewModel : ViewModel() {
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Corrected: Public getter for errorMessage
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // DriverID
    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId

    // Function to set the error message from outside the ViewModel
    fun setErrorMessage(message: String?) {
        _errorMessage.value = message
    }

    fun setScannedDriverId(driverId: String) {
        if (_scannedDriverId.value != driverId) {
            _scannedDriverId.value = driverId
            fetchAppointments(driverId)
        }
    }

    fun fetchAppointments(driverId: String? = _scannedDriverId.value) {
        if (driverId == null) {
            _appointments.value = emptyList()
            // Using setErrorMessage to update the public errorMessage
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null) // Clear previous errors on new fetch attempt
            try {
                val driverAppointments = RetrofitInstance.api.getAppointmentsByDriverId(driverId)
                _appointments.value = driverAppointments
            } catch (e: IOException) {
                setErrorMessage("Network error. Please check your connection and ensure the API is running.")
            } catch (e: HttpException) {
                setErrorMessage("API error: ${e.message()}")
            } catch (e: Exception) {
                setErrorMessage("An unexpected error occurred: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    //Toggle the checked state of a specific appointment
    fun toggleAppointmentChecked(appointmentId: String) {

        _appointments.value = _appointments.value.map { appointment ->
            if (appointment.id == appointmentId) {
                // Only allow toggling for "Pending" appointments
                if (appointment.status.equals("Pending", ignoreCase = true) || appointment.status.isEmpty()) { // <-- CHANGED LINE
                    appointment.copy(isChecked = !appointment.isChecked)
                } else {
                    appointment // Do not change checked state if not pending // <-- CHANGED LINE
                }
            } else {
                appointment
            }
        }
    }

    // New: Toggle the checked state of all appointments

    fun toggleSelectAll(selectAll: Boolean) {
        _appointments.value = _appointments.value.map { appointment ->
            // Only toggle if the appointment status is "Pending"
            if (appointment.status.equals("Pending", ignoreCase = true) || appointment.status.isEmpty()) { // <-- CHANGED LINE
                appointment.copy(isChecked = selectAll)
            } else {
                appointment // Keep non-pending appointments as they are // <-- CHANGED LINE
            }
        }
    }

    //Check-in selected appointments
    // New: Check-in selected appointments
    fun checkInSelectedAppointments() {
        viewModelScope.launch {
            val selectedAppointments = _appointments.value.filter { it.isChecked && it.status.equals("Pending", ignoreCase = true) || it.status.isEmpty() }
            if (selectedAppointments.isEmpty()) {
                setErrorMessage("No pending appointments selected for check-in.")
                return@launch
            }

            _isLoading.value = true
            setErrorMessage(null)
            var successCount = 0
            var errorOccurred = false

            for (appointment in selectedAppointments) {
                try {
                    val response = RetrofitInstance.api.checkInAppointment(appointment.id)
                    if (response.isSuccessful) {
                        successCount++
                    } else {
                        errorOccurred = true
                        setErrorMessage("Failed to check in appointment ${appointment.description}: ${response.code()} ${response.message()}")
                        // Break or continue based on desired behavior for multiple failures
                    }
                } catch (e: Exception) {
                    errorOccurred = true
                    setErrorMessage("Error checking in appointment ${appointment.description}: ${e.message}")
                    // Break or continue
                }
            }

            if(successCount > 0 && !errorOccurred){
                setErrorMessage("Successfully checked in $successCount appointment(s)")
            } else if(successCount > 0 && errorOccurred) {
                setErrorMessage("Some appointments checked in, but errors occurred with others. Please check logs for details.")
            } else if (successCount == 0 && errorOccurred) {
                if(errorMessage.value == null || errorMessage.value == "No pending appointments selected for check-in."){
                    setErrorMessage("Failed to check in all selected appointments.")
                }
            }

            fetchAppointments(_scannedDriverId.value) // Refresh the list after attempted check-ins
            _isLoading.value = false
        }
    }


    // This fetchAppointments is for the refresh button, using the current scanned ID
    fun fetchAppointments() {
        fetchAppointments(_scannedDriverId.value)
    }

    fun checkInAppointment(id: String) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.checkInAppointment(id)
                fetchAppointments(_scannedDriverId.value)
            } catch (e: Exception) {
                setErrorMessage("Failed to check in: ${e.message}")
            }
        }
    }
}


// =====================================================================================
// == MAIN ACTIVITY & UI (Jetpack Compose) ==
// =====================================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val viewModel: AppointmentViewModel = viewModel()
                LoginScreen(viewModel = viewModel
                )
               // AppointmentListScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentListScreen(viewModel: AppointmentViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    // Corrected: Collect errorMessage directly from ViewModel
    //state for selected appointments count

    val selectedAppointmentCount by remember(appointments){
        derivedStateOf {appointments.count { it.isChecked && it.status.equals("Pending", ignoreCase = true) || it.status.isEmpty()}}
    }

    //Derived state for "Select all" checkbox
    val allAppointmentsChecked by remember(appointments) {
        derivedStateOf {
            val pendingAppointments = appointments.filter { it.status.equals("Pending", ignoreCase = true) }

            // If there are no pending appointments, "Select All" cannot logically be "all checked".
            // If there are pending appointments, check if all of them are currently checked.
            pendingAppointments.isNotEmpty() && pendingAppointments.all { it.isChecked }
        }
    }







    val errorMessage by viewModel.errorMessage.collectAsState() // This line is correct now
    val scannedDriverId by viewModel.scannedDriverId.collectAsState()

    val context = LocalContext.current

    var showQrScanner by remember { mutableStateOf(false) }

    // Removed the problematic duplicate MutableStateFlow declaration
    // private val _errorMessageAppointment = MutableStateFlow<String?>(null)
    // val errorMessageAppointment: StateFlow<String?> = _errorMessage // This was causing conflict

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showQrScanner = true
        } else {
            Toast.makeText(context, "Camera permission denied. Cannot scan QR code.", Toast.LENGTH_LONG).show()
            viewModel.setErrorMessage("Camera permission denied. Cannot scan QR code.")
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Terminliste") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.fetchAppointments() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    if (scannedDriverId != null && !showQrScanner) { // Only show if a driver is scanned and scanner is not open
                        Button(
                            onClick = { viewModel.checkInSelectedAppointments() },
                            enabled = selectedAppointmentCount > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Check In Selected ($selectedAppointmentCount)")
                        }
                    }
                    IconButton(onClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> { // Fully qualified name
                                showQrScanner = true
                            }
                            else -> {
                                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "Scan QR Code"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showQrScanner) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeScanned = { barcodeValue ->
                        if (barcodeValue != null) {
                            viewModel.setScannedDriverId(barcodeValue)
                            showQrScanner = false
                        } else {
                            Toast.makeText(context, "QR code not detected or invalid.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    // Use the errorMessage collected from the ViewModel
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "An unknown error occurred.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    scannedDriverId == null -> {
                        Text(
                            text = "Bitte scannen Sie einen QR-Code, um Termine anzuzeigen.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    appointments.isEmpty() -> {
                        Text(
                            text = "Keine Termine für Fahrer-ID gefunden ${scannedDriverId ?: "N/A"}. Tippen Sie auf „Aktualisieren“, um es erneut zu versuchen.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // New: Select All Checkbox
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = allAppointmentsChecked,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleSelectAll(isChecked)
                                        },
                                        enabled = appointments.any { it.status.equals("Pending", ignoreCase = true) || it.status.isEmpty() } // Only enable if there are pending appointments
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
                                    appointment = appointment,
                                    isChecked = appointment.isChecked, // <--- FIXED: Removed the '!' here
                                    onCheckedChange = { checked ->

                                        viewModel.toggleAppointmentChecked(appointment.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun AppointmentItem(appointment: Appointment, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val isCompleted = appointment.status.equals("Completed", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Only show checkbox for pending appointments



            if (!isCompleted) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    enabled = !isCompleted,
                    // --- NEW / CHANGED CODE START ---
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Red, // Color when the checkbox is checked
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), // Default unchecked color
                        checkmarkColor = Color.White // Color of the checkmark itself (often white on a dark checked box)
                    ),
                    // --- NEW / CHANGED CODE END ---
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                // Optional: A placeholder or just empty space for completed appointments
                Spacer(modifier = Modifier.width(48.dp)) // Aligns content if checkbox were present
            }




            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = appointment.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,

                    )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Driver: ${appointment.driver?.name ?: "N/A"}")
                Text(text = "Date: ${formatDate(appointment.appointmentDateTime)}")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Status: ")
                    Text(
                        text = appointment.status,
                        color = if (isCompleted) Color(0xFF388E3C) else Color(0xFFFBC02D),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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


