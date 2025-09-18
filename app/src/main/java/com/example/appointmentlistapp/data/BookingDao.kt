package com.example.appointmentlistapp.data

import androidx.room.Dao
import androidx.room.Query
import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.Flow

/**
 * The Data Access Object (DAO) for the 'button_configs' table.
 * It provides methods for interacting with the database.
 */
@Dao
interface BookingDao {
    /**
     * Gets the button configurations for a specific client and screen from the database.
     * The result is returned as a Flow, which emits real-time updates whenever
     * the data in the database changes.
     *
     * @param clientId The ID of the client.
     * @param screenId The ID of the screen.
     * @return A Flow emitting a list of ButtonConfig objects.
     */
    @Query("SELECT * FROM button_configs WHERE clientId = :clientId AND screenId = :screenId")
    fun getButtonsForClientAndScreen(clientId: String, screenId: String): Flow<List<ButtonConfig>>

    // Optional: You can add other methods here for inserting or deleting data.
    // Room provides annotations like @Insert, @Update, and @Delete.
    // For example:
    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    // suspend fun insertAll(buttonConfigs: List<ButtonConfig>)
}