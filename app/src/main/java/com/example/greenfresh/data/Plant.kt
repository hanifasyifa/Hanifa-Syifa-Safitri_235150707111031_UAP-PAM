package com.example.greenfresh.data

import com.google.gson.annotations.SerializedName

data class Plant(
    @SerializedName("id")
    val id: Int,

    @SerializedName("plant_name")
    val plant_name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: String,

    @SerializedName("created_at")
    val created_at: String? = null,

    @SerializedName("updated_at")
    val updated_at: String? = null
)

