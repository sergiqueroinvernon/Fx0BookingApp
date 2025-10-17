package com.example.appointmentlistapp.data

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents the status of a logbook entry.
 */
enum class LogbookStatus {
    CONFIRMED,
    NOT_CONFIRMED,
}

/**
 * Represents the details of an approval action.
 * Corresponds to the "Genehmigung" section.
 *
 * @property approvedOn The date the entry was approved.
 * @property approvedBy The person who approved the entry.
 * @property notes Any notes related to the approval.
 */
data class Approval(
    val approvedOn: LocalDate?,
    val approvedBy: String?,
    val notes: String?
)

/**
 * Represents the details of a cancellation action.
 * Corresponds to the "Storno" section.
 *
 * @property cancelledOn The date the entry was cancelled.
 * @property cancelledBy The person who cancelled the entry.
 * @property reason The reason for the cancellation.
 */
data class Cancellation(
    val cancelledOn: LocalDate?,
    val cancelledBy: String?,
    val reason: String?
)

/**
 * Represents a single leg or sub-route of a larger trip.
 * Corresponds to the "Teilstrecken" section.
 *
 * @property startTime The start time of the trip leg.
 * @property startLocation The starting location of the trip leg.
 * @property endTime The end time of the trip leg.
 * @property endLocation The ending location of the trip leg.
 */
data class TripLeg(
    val startTime: LocalDateTime,
    val startLocation: String,
    val endTime: LocalDateTime,
    val endLocation: String
)

/**
 * Represents a complete logbook entry, corresponding to a single row in the main table
 * and the detailed view.
 *
 * @property id The unique identifier for the entry (e.g., 1000351477).
 * @property status The current status of the entry (e.g., Bestätigt, Nicht bestätigt).
 * @property startTime The start date and time of the trip.
 * @property endTime The end date and time of the trip.
 * @property vehicle The vehicle used for the trip (e.g., D-WG 466E).
 * @property internalNumber The internal reference number.
 * @property startOdometer The odometer reading at the start of the trip in kilometers.
 * @property endOdometer The odometer reading at the end of the trip in kilometers.
 * @property distance The total distance of the trip in kilometers.
 * @property purpose The purpose of the trip (Reisezweck).
 * @property startLocation The starting location of the overall trip.
 * @property isResponseTimeTrip A flag indicating if this was a "Reaktionszeitfahrt".
 * @property justification The justification or reason for the trip (Begründung).
 * @property tripLegs A list of sub-routes or legs for the trip.
 * @property approval Optional details about the entry's approval. Null if not approved.
 * @property cancellation Optional details about the entry's cancellation. Null if not cancelled.
 * @property notes General notes or comments for the entry.
 */
data class Logbook(
    val entryNr: Long, // Changed from 'id' to 'entryNr'
    val status: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val vehicle: Vehicle,
    val internalNumber: String?, // Added from the table image ('Interne Nr.')
    val startOdometer: Double,
    val endOdometer: Double,
    val purposeOfTrip: String?,
    val distance: Double,
    val startLocation: String,
    val endLocation: String,
    // Note: 'isReactionTimeDriver' was in original but not in table. Keeping for context.
    val isReactionTimeDriver: Boolean,
    val isResponseTimeTrip: Boolean,
    val justification: String?,
    val tripLegs: List<TripLeg>,
    val approval: Approval?,
    val cancellation: Cancellation?,
    val notes: String?,
    // Added for UI selection logic (like the Booking model had)
    val isChecked: Boolean = false
)

