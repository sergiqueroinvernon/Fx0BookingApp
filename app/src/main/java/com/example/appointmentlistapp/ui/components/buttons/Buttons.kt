package com.example.appointmentlistapp.ui.components.buttons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.appointmentlistapp.R

class Buttons {
    @Composable
    fun InfoButton(showDetails: Boolean, onToggleDetails: () -> Unit) {
        Button(onClick = onToggleDetails) {
            Icon(
                painter = painterResource(id = R.drawable.info),
                contentDescription = "Info Icon",
                modifier = Modifier.size(24.dp)
            )
            // Change button text based on the state
            Text(if (showDetails) "Hide Details" else "Show Details")
        }
    }
}