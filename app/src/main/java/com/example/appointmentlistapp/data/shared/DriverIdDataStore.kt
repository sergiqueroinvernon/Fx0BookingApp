// File: com.example.appointmentlistapp.data.persistence/DriverIdDataStore.kt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Property delegate to create the singleton DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "driver_prefs")

/**
 * Manages the persistent storage and retrieval of the scanned Driver ID.
 * This class belongs to the Data Layer.
 */
class DriverIdDataStore(private val context: Context) {

    // 2. Define the key for storing the ID as a string preference
    private val DRIVER_ID_KEY = stringPreferencesKey("driver_id_key")

    /** * Reads the Driver ID as a Kotlin Flow (async and reactive).
     * The ViewModel will collect this Flow to get the persisted ID.
     */
    val driverIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            // Returns the stored ID, or null if the app is launched for the first time
            preferences[DRIVER_ID_KEY]
        }

    /** * Saves the provided Driver ID persistently on the device.
     */
    suspend fun saveDriverId(driverId: String) {
        context.dataStore.edit { preferences ->
            preferences[DRIVER_ID_KEY] = driverId
        }
    }

    /** * Clears the stored ID, typically used when the user logs out.
     */
    suspend fun clearDriverId() {
        context.dataStore.edit { preferences ->
            preferences.remove(DRIVER_ID_KEY)
        }
    }
}