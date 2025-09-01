package com.example.appointmentlistapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appointmentlistapp.AppointmentViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

// This function should ideally be in a separate file (e.g., in a data or model layer)
// to follow a cleaner architecture.
fun login(email: String, password: String): Boolean {
    return email == "s.quero@fleetone.de" && password == "Catalunya2025"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AppointmentViewModel, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    val customerProfileImageUrl =
        "https://www.designyourway.net/blog/wp-content/uploads/2019/04/BMW-wallpaper-4-1250x781.jpg"

    // Correctly watch the state to show the snackbar
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = snackbarMessage,
                    duration = SnackbarDuration.Long
                )
                showSnackbar = false // Reset the state
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = customerProfileImageUrl,
                contentDescription = "Customer profile photo background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            Column(
                modifier = Modifier
                    .width(600.dp)
                    .background(Color.White)
                    .padding(36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Fuhrparkmanagement für Stadtwerke Düsseldorf AG",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kennwort") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isEmpty() || password.isEmpty()) {
                            snackbarMessage = "E-Mail oder Kennwort fehlt."
                            showSnackbar = true
                        } else {
                            // Correctly call the login function with user input
                            if (login(email, password)) {
                                onLoginSuccess()
                            } else {
                                snackbarMessage = "E-Mail und/oder Kennwort ungültig."
                                showSnackbar = true
                            }
                        }
                    },
                    modifier = Modifier
                        .width(250.dp)
                        .height(50.dp)
                        .border(1.dp, Color.Black, shape = RectangleShape),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Anmelden")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val viewModel = remember { AppointmentViewModel() }
    LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = {}
    )
}