package com.example.appointmentlistapp.data.api

import com.example.appointmentlistapp.data.Logbook
import com.example.appointmentlistapp.data.Vehicle // Placeholder for Vehicle data class
import com.example.appointmentlistapp.data.PurposeOfTrip // Placeholder for PurposeOfTrip data class
import com.example.appointmentlistapp.data.StatusOption // Placeholder for StatusOption data class
import com.example.appointmentlistapp.data.model.Driver

import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Retrofit Service Interface for the LogBooks API endpoints.
 * This service focuses on retrieving vehicle trip logs and associated lookup data.
 */
interface LogbookAppService {

    /**
     * GET /api/LogBooks
     * Retrieves a list of all logbook entries.
     */
    @GET("api/LogBooks")
    suspend fun getLogbooks(): List<Logbook>

    /**
     * GET /api/LogBooks/driver/{driverId}
     * Retrieves a list of logbook entries for a specific driver.
     */
    @GET("api/LogBooks/driver/{driverId}")
    suspend fun getLogbooksByDriverId(@Path("driverId") driverId: String): List<Logbook>

    /**
     * GET /api/LogBooks/fullview/driver/{driverId}
     * Retrieves a detailed (full view) list of logbook entries for a specific driver.
     */
    @GET("api/LogBooks/fullview/driver/{driverId}")
    suspend fun getFullLogbooksByDriverId(@Path("driverId") driverId: String): List<Logbook>

    /**
     * GET /api/LogBooks/vehicles/{driverId}
     * Retrieves the vehicles associated with a specific driver for logbook context.
     */
    @GET("api/LogBooks/vehicles/{driverId}")
    suspend fun getVehiclesByDriverId(@Path("driverId") driverId: String): List<Vehicle>

    /**
     * GET /api/LogBooks/drivers
     * Retrieves a list of all drivers in the logbook context.
     */
    @GET("api/LogBooks/drivers")
    suspend fun getLogbookDrivers(): List<Driver>

    /**
     * GET /api/LogBooks/purposeOfTrips
     * Retrieves a list of available trip purposes for logbooks.
     */
    @GET("api/LogBooks/purposeOfTrips")
    suspend fun getLogbookPurposeOfTrips(): List<PurposeOfTrip>

    /**
     * GET /api/LogBooks/statusOptions
     * Retrieves a list of available status options for logbooks.
     */
    @GET("api/LogBooks/statusOptions")
    suspend fun getLogbookStatusOptions(): List<StatusOption>


    // NOTE: Based on the OpenAPI file, only GET methods were defined for /api/LogBooks.
    // If you need POST/PUT/DELETE functionality (like creating or updating a Logbook),
    // you would add those endpoints here, assuming they exist on the server side:

    /*
    @POST("api/LogBooks")
    suspend fun createLogbook(@Body logbook: Logbook): Logbook

    @PUT("api/LogBooks/{id}")
    suspend fun updateLogbook(@Path("id") id: String, @Body logbook: Logbook): Logbook
    */

    companion object
}
