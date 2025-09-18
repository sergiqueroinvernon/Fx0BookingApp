package com.example.appointmentlistapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao

abstract class AppDataBase:  RoomDatabase() {

    @Database(entities = [Booking::class], version = 1, exportSchema = false)
    abstract class AppDataBase : RoomDatabase() {
        //Abstract method to get the DAO interface
        abstract fun bookingDao(): BookingDao
        /**
         * This is the Singleton pattern implementation. It ensures that only one
         * instance of the database is created across the entire app.
         */
        companion object {
            // The @Volatile annotation ensures that changes made by one thread
            // are immediately visible to all other threads.
            @Volatile
            private var INSTANCE: AppDataBase? = null
            /**
             * Gets the single instance of the database. If it does not exist,
             * it creates one in a thread-safe manner.
             */
            fun getDatabase(context: Context): AppDataBase {
                return INSTANCE ?: synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java,
                        "app_database"
                    ).build()
                    INSTANCE = instance
                    instance
                }
            }
        }



}

}