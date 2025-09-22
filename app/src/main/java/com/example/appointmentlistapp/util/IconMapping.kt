package com.example.appointmentlistapp.util

import androidx.compose.runtime.Composable

import com.example.appointmentlistapp.R


@Composable
fun getIconForType(type: String?): Int {
    return when (type) {
        "add" -> R.drawable.add
        "approve" -> R.drawable.approve
        "cancel" -> R.drawable.cancel
        "confirm" -> R.drawable.confirm
        "copy" -> R.drawable.copy
        "edit" -> R.drawable.edit
        "filter" -> R.drawable.filter
        "finish" -> R.drawable.finish
        "info" -> R.drawable.info
        "layout" -> R.drawable.layout
        "start" -> R.drawable.start
        else -> { R.drawable.start}
    }
}