package com.example.appointmentlistapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

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

    //Retrieves all bookings from the database
    //The result is returned as a Flow, which emits real-time updates whenever the data
    //In the database changes
    //@return A Flow emitting a list of all Booking objects

    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC")
    fun getAllBookings(): Flow<List<Booking>>

    //Inserts one or more bookings in to thedatabase. THe 'onconflict' strategy handles cases where a booking with the same
    //primary key already exists

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId" )
    suspend fun getBookingById(bookingId: Int): Booking?

    @Query("DELETE FROM bookings WHERE bookingId = :bookingId")
    suspend fun deleteBookingById(bookingId: Int)

    @Update
    suspend fun updateBooking(booking: Booking)
}