package com.example.appointmentlistapp.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.appointmentlistapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime



data class LogBookUiState(
    val entries: List<LogbookEntry> = emptyList(),
    val selectedEntry: LogbookEntry? = null,
    val checkedEntryIds: Set<Long> = emptySet(),
    val isDetailPlainVisible: Boolean = false,
    val isFilterPlaneVisible: Boolean = false,
    val isInEditMode: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
class LogBookViewModel : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    private val _logbookEntries = MutableStateFlow(
        listOf(
            LogbookEntry(
                id = 1000351477,
                status = LogbookStatus.CONFIRMED,
                startTime = LocalDateTime.of(2025, 7, 21, 13, 33),
                endTime = LocalDateTime.of(2025, 7, 21, 13, 45),
                vehicle = Vehicle(licensePlate = "D-WG 466E", internNumber = "234234"),
                startOdometer = 7974.0,
                endOdometer = 7984.0,
                purpose = "Kundenbesuch",
                distance = 10.0,
                startLocation = "Firmenzentrale, Düsseldorf",
                isReactionTimeDriver = false,
                isResponseTimeTrip = false,
                justification = null,
                tripLegs = emptyList(),
                approval = Approval(
                    approvedOn = LocalDate.of(2025, 7, 22),
                    approvedBy = "Max Mustermann",
                    notes = "Fahrt genehmigt."
                ),
                cancellation = null,
                notes = "Keine besonderen Vorkommnisse."
            ),
            LogbookEntry(
                id = 1000351454,
                status = LogbookStatus.NOT_CONFIRMED,
                startTime = LocalDateTime.of(2025, 7, 21, 12, 48),
                endTime = LocalDateTime.of(2025, 7, 21, 13, 15),
                vehicle = Vehicle(licensePlate = "D-EM 719E",  internNumber ="DDAFD"),
                startOdometer = 7964.0,
                endOdometer = 7974.0,
                purpose = "Baustelle",
                distance = 10.0,
                startLocation = "Lager, Köln",
                isReactionTimeDriver = true,
                isResponseTimeTrip = true,
                justification = "Dringender Materialtransport",
                tripLegs = listOf(
                    TripLeg(
                        startTime = LocalDateTime.of(2025, 7, 21, 12, 48),
                        startLocation = "Lager, Köln",
                        endTime = LocalDateTime.of(2025, 7, 21, 13, 15),
                        endLocation = "Baustelle A, Köln"
                    )
                ),
                approval = null,
                cancellation = null,
                notes = null
            )
        )
    )
    @RequiresApi(Build.VERSION_CODES.O)
    val logbookEntries: StateFlow<List<LogbookEntry>> = _logbookEntries.asStateFlow()

    private val _selectedEntry = MutableStateFlow<LogbookEntry?>(null)
    val selectedEntry: StateFlow<LogbookEntry?> = _selectedEntry.asStateFlow()

    private val _checkedEntryIds = MutableStateFlow<Set<Long>>(emptySet())
    val checkedEntryIds: StateFlow<Set<Long>> = _checkedEntryIds.asStateFlow()

    init {
        // Set the first entry as selected by default
        _selectedEntry.value = _logbookEntries.value.firstOrNull()
    }

    fun selectEntry(entry: LogbookEntry) {
        _selectedEntry.value = entry
    }

    fun toggleEntryChecked(entryId: Long) {
        val currentChecked = _checkedEntryIds.value.toMutableSet()
        if (currentChecked.contains(entryId)) {
            currentChecked.remove(entryId)
        } else {
            currentChecked.add(entryId)
        }
        _checkedEntryIds.value = currentChecked
    }

    //Private mutable state
    private val _uiState = MutableStateFlow(LogBookUiState())
    //Public immutable state that the UI can observe

    //Entry Selected
    fun onEntrySelected(entry: LogbookEntry){
        _uiState
    }


}
