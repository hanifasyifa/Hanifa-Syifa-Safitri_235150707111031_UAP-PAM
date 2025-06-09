package com.example.greenfresh.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T? = null
)

data class PlantListResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Plant>
)

data class SinglePlantResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Plant
)