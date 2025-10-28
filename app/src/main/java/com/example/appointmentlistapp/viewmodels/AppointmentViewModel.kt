package com.example.appointmentlistapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appointmentlistapp.data.model.Appointment
import com.example.appointmentlistapp.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
                val driverAppointments = RetrofitInstance.bookingApi.getAppointmentsByDriverId(driverId)
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
                    val response = RetrofitInstance.bookingApi.checkInAppointment(appointment.id)
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