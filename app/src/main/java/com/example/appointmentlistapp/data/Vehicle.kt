package com.example.appointmentlistapp.data

import com.google.gson.annotations.SerializedName

data class Vehicle(
    @SerializedName("Id")
    val id: Int,

    // 2. Propietat simple
    @SerializedName("Registration")
    // ATENCIÓ: Si C# no té [Required], pot ser null. Més segur definir-ho com a nul·lable.
    val registration: String?,

    // 3. Foreign Key (FK) o propietat simple
    @SerializedName("PoolId")
    val poolId: Int
)

