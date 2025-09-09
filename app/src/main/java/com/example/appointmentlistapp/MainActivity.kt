package com.example.appointmentlistapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appointmentlistapp.ui.screens.BookingScreen
import com.example.appointmentlistapp.ui.screens.LogbookScreen
import com.example.appointmentlistapp.ui.screens.LogbookScreenCheck
import com.example.appointmentlistapp.ui.screens.LoginScreen
import com.example.appointmentlistapp.ui.viewmodel.BookingViewModel
import com.example.appointmentlistapp.ui.viewmodel.LogBookViewModel

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// DATA MODELS
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

// CAMERA PREVIEW AND BARCODE ANALYZER
typealias BarcodeAnalyserListener = (barcodeValue: String?) -> Unit

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onBarcodeScanned: BarcodeAnalyserListener
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

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

// Login function moved to a shared location for use by LoginScreen
fun login(email: String, password: String): Boolean {
    return email == "s.quero@fleetone.de" && password == "Catalunya2025!"
}

// NETWORKING LAYER (Retrofit)
private const val BASE_URL = "http://172.26.140.23:5251/"

interface ApiService {
    @GET(value = "api/appointments/driver/{driverId}")
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

// VIEWMODEL (To hold state and handle logic)
class AppointmentViewModel : ViewModel() {
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _scannedDriverId = MutableStateFlow<String?>(null)
    val scannedDriverId: StateFlow<String?> = _scannedDriverId

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
        if (driverId.isNullOrBlank()) {
            _appointments.value = emptyList()
            setErrorMessage("Bitte scannen Sie einen QR-Code, um Fahrertermine zu erhalten.")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            setErrorMessage(null)
            try {
                val driverAppointments = RetrofitInstance.api.getAppointmentsByDriverId(driverId)
                _appointments.value = driverAppointments
            } catch (e: IOException) {
                setErrorMessage("Netzwerkfehler. Bitte überprüfen Sie Ihre Verbindung und stellen Sie sicher, dass die API läuft.")
            } catch (e: HttpException) {
                setErrorMessage("API-Fehler: ${e.message()}")
            } catch (e: Exception) {
                setErrorMessage("Ein unerwarteter Fehler ist aufgetreten: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleAppointmentChecked(appointmentId: String) {
        _appointments.value = _appointments.value.map { appointment ->
            if (appointment.id == appointmentId && appointment.status.equals("Pending", ignoreCase = true)) {
                appointment.copy(isChecked = !appointment.isChecked)
            } else {
                appointment
            }
        }
    }

    fun toggleSelectAll(selectAll: Boolean) {
        _appointments.value = _appointments.value.map { appointment ->
            if (appointment.status.equals("Pending", ignoreCase = true)) {
                appointment.copy(isChecked = selectAll)
            } else {
                appointment
            }
        }
    }

    fun checkInSelectedAppointments() {
        viewModelScope.launch {
            val selectedAppointments = _appointments.value.filter { it.isChecked && it.status.equals("Pending", ignoreCase = true) }
            if (selectedAppointments.isEmpty()) {
                setErrorMessage("Keine ausstehenden Termine für den Check-in ausgewählt.")
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
                        setErrorMessage("Fehler beim Einchecken des Termins ${appointment.description}: ${response.code()} ${response.message()}")
                    }
                } catch (e: Exception) {
                    errorOccurred = true
                    setErrorMessage("Fehler beim Einchecken des Termins ${appointment.description}: ${e.message}")
                }
            }

            if (successCount > 0 && !errorOccurred) {
                setErrorMessage("Erfolgreich $successCount Termin(e) eingecheckt.")
            } else if (successCount > 0 && errorOccurred) {
                setErrorMessage("Einige Termine wurden eingecheckt, aber bei anderen sind Fehler aufgetreten. Bitte überprüfen Sie die Protokolle auf Details.")
            } else if (successCount == 0 && errorOccurred) {
                setErrorMessage("Alle ausgewählten Termine konnten nicht eingecheckt werden.")
            }
            fetchAppointments(_scannedDriverId.value)
            _isLoading.value = false
        }
    }
}

// MAIN ACTIVITY & UI (Jetpack Compose)
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                val viewModel: AppointmentViewModel = viewModel()

                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") { // Start with "login"
                    composable("login") {
                        LoginScreen(
                            // ERROR 1 FIX: You were missing the viewModel parameter.
                            viewModel = viewModel,

                            // ERROR 2 FIX: The onLoginSuccess lambda was missing.
                            onLoginSuccess = {
                                navController.navigate("main") { // Navigate to "main", not "appointmentList"
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainAppScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppointmentViewModel) {
    val tabs = listOf("Start", "Meine Buchungen", "Mein Fahrtenbuch", "Fahrtenbuchprüfung")
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to "Terminliste"

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
                                        contentDescription = "Start Tab Icon" ,
                                        modifier = Modifier.size(25.dp)// Add the contentDescription here
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
                3 -> LogbookScreenCheck()
            }
        }
    }
}



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

    // A single Box now holds all the content of the screen.
    // The Scaffold and TopAppBar are removed.
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                // Actions bar specific to the AppointmentListScreen
               CenterAlignedTopAppBar(
                    title = {  },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.fetchAppointments() }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh"
                            )
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
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED -> {
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

                // Main content
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage ?: "Ein unbekannter Fehler ist aufgetreten.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }
                    scannedDriverId == null -> {
                        Column(
                            modifier = Modifier
                                .border(1.dp, Color.Black, RectangleShape)
                                .fillMaxSize()
                                .fillMaxHeight()
                                .background(Color(0xFFF8F8F8))

                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(15.dp)
                                    .background(Color.White)
                                    .border(1.dp, Color.Black, RectangleShape),

                            ) {


                                Text(
                                    text = "Herzlich willkommen beim Mobilitätsportal der Fuhpark-Demo AG!\n",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(15.dp)


                                )
                                Text(
                                    text = "Hier finden Sie alle unsere Angebote rund um die Themen Fuhrpark, Car Sharing und Mobilität.\n" +
                                            "Für Fragen und Kritik haben wir immer ein offenes Ohr.",
                                    modifier = Modifier.padding(15.dp)
                                    )
                            }


                        }


                            Text(
                                text = "Bitte scannen Sie einen QR-Code, um Termine anzuzeigen.",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(100.dp)
                            )
                        }


                    appointments.isEmpty() -> {
                        Text(
                            text = "Keine Termine für Fahrer-ID ${scannedDriverId ?: "N/A"} gefunden. Tippen Sie auf „Aktualisieren“, um es erneut zu versuchen.",
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
                                    appointment = appointment,
                                    isChecked = appointment.isChecked,
                                    onCheckedChange = {
                                        viewModel.toggleAppointmentChecked(appointment.id)
                                    }
                                )

                        }}
                    }
                }
            }
        }
    }
}


@Composable
fun AppointmentItem(appointment: Appointment, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val isPending = appointment.status.equals("Pending", ignoreCase = true)
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
            if (isPending) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Red,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(48.dp))
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