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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AppointmentViewModel) {
    // State for the email and password fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    //Showing Snackbar
    var snackbarHostState = remember { SnackbarHostState() }
    var coroutineScope = rememberCoroutineScope()

    //Dynamic profile photo
    val customerProfileImageUrl =
        "https://www.designyourway.net/blog/wp-content/uploads/2019/04/BMW-wallpaper-4-1250x781.jpg"


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {

        AsyncImage(
            model = customerProfileImageUrl,
            contentDescription = "Customer profile photo background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Semi-transparent Overlay to make text readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
        )


        Column(
            modifier = Modifier
                .width(600.dp)
                .background(Color.White)
                .padding(36.dp), // Padding inside the column, between the content and the edges

            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                text = "Fuhrparkmanagement für Stadtwerke Düsseldorf AG",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Email Text Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-Mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Text Field
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

            // Login Button
            Button(
                onClick = {
                    // Handle login logic here, e.g., validate input and authenticate
                    if(email.isEmpty()  && password.isEmpty()){
                    errorMessage = "E-mail oder Kennwort fehlt."
                    }
                    if (email.isEmpty()) {
                        errorMessage = "E-mail fehlt."
                    }
                    else if(password.isEmpty())
                            {
                       errorMessage = "Kennwort fehlt."
                    }

                    else {
                        errorMessage = null
                        println("Login clicked with Email: $email, Password: $password.")
                    }


                },
                modifier = Modifier
                    .width(250.dp)
                    .height(35.dp)
                    .border(1.dp, Color.Black, shape = RectangleShape)
                    .background(Color.White),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Anmelden")
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth()
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.White, // CUSTOM: set to white
                    contentColor = Color.Black // CUSTOM: set content to black
                )
            }
        }
    }

    LaunchedEffect(errorMessage)
    {
        if (errorMessage != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage!!,
                    duration = SnackbarDuration.Long
                )
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // It's good practice to wrap previews in your app's theme
    // AppTheme {
    LoginScreen(
        viewModel = TODO()
    )
    // }
}