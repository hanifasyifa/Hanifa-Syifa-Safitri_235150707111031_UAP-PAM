package com.example.greenfresh.repository

import com.example.greenfresh.api.ApiClient
import com.example.greenfresh.data.Plant
import com.example.greenfresh.data.PlantRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class PlantRepository {

    private val apiService = ApiClient.plantApiService

    suspend fun getAllPlants(): Result<List<Plant>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllPlants()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.data.isNotEmpty()) {
                        Result.success(body.data)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Data tanaman tidak ditemukan"
                        500 -> "Server error, silakan coba lagi nanti"
                        else -> "Gagal mengambil data: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun getPlantByName(plantName: String): Result<Plant> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedPlantName = URLEncoder.encode(plantName, "UTF-8")
                val response = apiService.getPlantByName(encodedPlantName)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Data tanaman tidak ditemukan"))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Tanaman dengan nama '$plantName' tidak ditemukan"
                        500 -> "Server error, silakan coba lagi nanti"
                        else -> "Gagal mengambil data: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun createPlant(plantRequest: PlantRequest): Result<Plant> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPlant(plantRequest)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Gagal membuat tanaman"))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Data yang dikirim tidak valid"
                        409 -> "Tanaman dengan nama tersebut sudah ada"
                        422 -> "Validasi gagal, periksa kembali data yang diinput"
                        500 -> "Server error, silakan coba lagi nanti"
                        else -> "Gagal menambahkan tanaman: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun updatePlant(plantName: String, plantRequest: PlantRequest): Result<Plant> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedPlantName = URLEncoder.encode(plantName, "UTF-8")
                val response = apiService.updatePlant(encodedPlantName, plantRequest)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.data != null) {
                        Result.success(body.data)
                    } else {
                        Result.failure(Exception("Gagal mengupdate tanaman"))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Data yang dikirim tidak valid"
                        404 -> "Tanaman dengan nama '$plantName' tidak ditemukan"
                        422 -> "Validasi gagal, periksa kembali data yang diinput"
                        500 -> "Server error, silakan coba lagi nanti"
                        else -> "Gagal mengupdate tanaman: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun deletePlant(plantName: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val encodedPlantName = URLEncoder.encode(plantName, "UTF-8")
                val response = apiService.deletePlant(encodedPlantName)

                if (response.isSuccessful) {
                    val body = response.body()
                    Result.success(body?.message ?: "Tanaman berhasil dihapus")
                } else {
                    val errorMessage = when (response.code()) {
                        404 -> "Tanaman dengan nama '$plantName' tidak ditemukan"
                        500 -> "Server error, silakan coba lagi nanti"
                        else -> "Gagal menghapus tanaman: ${response.message()}"
                    }
                    Result.failure(Exception(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(handleException(e))
            }
        }
    }

    private fun handleException(e: Exception): Exception {
        return when {
            e.message?.contains("UnknownHostException") == true ||
                    e.message?.contains("ConnectException") == true -> {
                Exception("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
            e.message?.contains("SocketTimeoutException") == true ||
                    e.message?.contains("timeout") == true -> {
                Exception("Koneksi timeout. Silakan coba lagi.")
            }
            e.message?.contains("SSLException") == true -> {
                Exception("Masalah keamanan koneksi. Silakan coba lagi.")
            }
            else -> {
                Exception("Terjadi kesalahan: ${e.message ?: "Unknown error"}")
            }
        }
    }
}