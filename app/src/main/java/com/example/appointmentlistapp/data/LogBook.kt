package com.example.appointmentlistapp.data

import com.example.appointmentlistapp.data.model.Driver // Assegura't que la teva classe Driver estigui definida
import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime

// 1. DEFINICIONS DE CLASSE DE DADES NECESSÀRIES

// Has de definir la classe Vehicle perquè el C# model la té com a propietat de navegació


// Aquestes classes es mantenen ja que són estructures internes sense canvis a la definició
data class TripLeg(
    val startTime: LocalDateTime,
    val startLocation: String,
    val endTime: LocalDateTime,
    val endLocation: String
)

data class Approval(
    val approvedOn: LocalDate?,
    val approvedBy: String?,
    val notes: String?
)

data class Cancellation(
    val cancelledOn: LocalDate?,
    val cancelledBy: String?,
    val reason: String?
)

/**
 * Model de Logbook Sincronitzat amb l'API de C# LogbookEntry
 */
data class Logbook(

    // 1. CLAU PRIMÀRIA & Claus Foranes (FKs)
    @SerializedName("entryId")
    val entryId: Long,

    @SerializedName("Driver")
    val driver: Driver? = null,


    @SerializedName("driverId")
    val driverId: String,       // Mapeig: C# Guid -> Kotlin String

    @SerializedName("vehicleId")
    val vehicleId: Int,

    @SerializedName("purposeOfTripId")
    val purposeOfTripId: Int,

    // 2. CAMPS DE DADES SIMPLES
    @SerializedName("status")
    val status: String,

    @SerializedName("internalNumber")
    val internalNumber: String?, // Nullable

    @SerializedName("purposeOfTrip")
    val purposeOfTrip: String?,  // Nullable

    @SerializedName("StartLocation")
    val startLocation: String,

    @SerializedName("endLocation")
    val endLocation: String,

    @SerializedName("isReactionTimeDriver")
    val isReactionTimeDriver: Boolean,

    @SerializedName("isResponseTimeTrip")
    val isResponseTimeTrip: Boolean,

    @SerializedName("justification")
    val justification: String?,  // Nullable

    @SerializedName("notes")
    val notes: String?,          // Nullable

    // 3. CAMPS TEMPORALS (C# DateTime -> Kotlin LocalDateTime)
    // Requereix un TypeAdapter per a Gson/Moshi
    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String,

    // 4. CAMPS NUMÈRICS (C# Decimal -> Kotlin Double)
    @SerializedName("startOdometer")
    val startOdometer: Double,

    @SerializedName("endOdometer")
    val endOdometer: Double,

    @SerializedName("distance")
    val distance: Double,

    // 5. PROPIETATS DE NAVEGACIÓ (Pot ser nul si el JSON no inclou l'objecte)

    @SerializedName("vehicle")
    val vehicle: Vehicle? = null,

    // 6. ESTAT LOCAL DE LA UI (C# [NotMapped])
    @SerializedName("isChecked") // S'afegeix per seguretat, tot i que hauria de ser ignorat
    val isChecked: Boolean = false
)