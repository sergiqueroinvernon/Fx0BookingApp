package com.example.appointmentlistapp.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class Driver(
    @SerializedName("Id")
    val id: String, // Mapeig: C# Guid -> Kotlin String

    // 2. CAMPS REQUERITS (NOT NULL)
    @SerializedName("Name")
    val name: String,

    @SerializedName("Email")
    val email: String,

    // 3. CAMPS TEMPORALS (C# DateTime -> Kotlin LocalDateTime)
    // ATENCIÓ: Requereix un TypeAdapter per a Gson/Moshi per analitzar el format ISO 8601.
    @SerializedName("CreatedAt")
    val createdAt: LocalDateTime,

    @SerializedName("UpdatedAt")
    val updatedAt: LocalDateTime

)