package com.example.greenfresh.data

import com.google.gson.annotations.SerializedName

data class PlantRequest(
    @SerializedName("plant_name")
    val plant_name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: String
)