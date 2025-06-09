package com.example.greenfresh.api

import com.example.greenfresh.data.*
import retrofit2.Response
import retrofit2.http.*

interface PlantApiService {
    @GET("plant/all")
    suspend fun getAllPlants(): Response<PlantListResponse>

    @GET("plant/{plant_name}")
    suspend fun getPlantByName(@Path("plant_name") plantName: String): Response<SinglePlantResponse>

    @POST("plant/new")
    suspend fun createPlant(@Body plantRequest: PlantRequest): Response<SinglePlantResponse>

    @PUT("plant/{plant_name}")
    suspend fun updatePlant(
        @Path("plant_name") plantName: String,
        @Body plantRequest: PlantRequest
    ): Response<SinglePlantResponse>

    @DELETE("plant/{plant_name}")
    suspend fun deletePlant(@Path("plant_name") plantName: String): Response<ApiResponse<Any>>
}