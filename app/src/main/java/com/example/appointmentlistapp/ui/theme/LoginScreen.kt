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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: AppointmentViewModel) {
    // State for the email and password fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    //Dynamic profile photo
    val customerProfileImageUrl = "https://www.designyourway.net/blog/wp-content/uploads/2019/04/BMW-wallpaper-4-1250x781.jpg"


    Box(
        modifier = Modifier.width(1000.dp),
        contentAlignment = Alignment.Center
    )
    {

        AsyncImage(
            model = customerProfileImageUrl,
            contentDescription = "Customer profile photo background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        Column(
            modifier = Modifier
                .width(500.dp)
                .padding(16.dp)
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
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
                    println("Login clicked with Email: $email, Password: $password")
                },
                modifier = Modifier.width(250.dp).height(35.dp)
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