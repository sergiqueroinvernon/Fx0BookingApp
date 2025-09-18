package com.example.appointmentlistapp.data

import com.example.appointmentlistapp.data.components.ButtonConfig
import kotlinx.coroutines.flow.Flow

//Repository -> Provide/Transform the data to the ViewModel. Intermediary between UI and Data Sources./Interact with the Networking Service
class BookingRepository(private val bookingDao: BookingDao) {
    /*Retrieves the button configurations for a specific client and screen
    *
    * This method delegates the data request to the BookingDao. In a more complex, this method could also contain login to:
    *  1-Check a local cache or a remote API FIRST
    * 2. Handle network requests and save the results to the database
    * 3. Handle data conflicts or syncronization
    *
    * The Flow return type ensures that the UI will get real-time updates whenever the underlying database data changes
    *@param clientId the unique ID of the client
    * @param screenId the unique ID of the screen
    * @return A flow of a List of ButtonConfig objects
    * */

    fun getButtonsForClientsAndScreen(clientId: String, screenId: String): Flow<List<ButtonConfig>>{
        return bookingDao.getButtonsForClientAndScreen(clientId, screenId)
    }



}