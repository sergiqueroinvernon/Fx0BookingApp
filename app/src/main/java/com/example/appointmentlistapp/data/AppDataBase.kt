package com.example.appointmentlistapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import com.example.appointmentlistapp.data.components.ButtonConfig

/**
 * The main database class. It serves as the primary access point to the
 * app's persisted data. It defines the database schema and provides a way
 * to access the DAOs for executing database queries.
 *
 * @Database: An annotation that defines the entities, version, and name of the database.
 * - entities: A list of classes that represent database tables.
 * - version: The database version number. It must be incremented whenever
 * you change the database schema.
 * - exportSchema: It's good practice to set this to false to avoid
 * a build warning unless you need to export the schema for migrations.
 */
@Database(entities = [ButtonConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Defines an abstract method for each DAO in the database.
     * Room will automatically generate the implementation for this method.
     */
    abstract fun bookingDao(): BookingDao

    /**
     * This is the Singleton pattern implementation. It ensures that only one
     * instance of the database is created across the entire app.
     */
    companion object {

        // The @Volatile annotation ensures that changes made by one thread
        // are immediately visible to all other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the single instance of the database. If it does not exist,
         * it creates one in a thread-safe manner.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If INSTANCE is not null, return it.
            // If it is null, create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // The name of the database file.
                )
                    .build()
                INSTANCE = instance
                // Return the created instance.
                instance
            }
        }
    }
}