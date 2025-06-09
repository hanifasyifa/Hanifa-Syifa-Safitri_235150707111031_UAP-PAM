package com.example.greenfresh

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenfresh.adapter.PlantAdapter
import com.example.greenfresh.data.Plant
import com.example.greenfresh.databinding.ActivityHomepageBinding
import com.example.greenfresh.repository.PlantRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomePageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomepageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var plantAdapter: PlantAdapter
    private lateinit var plantRepository: PlantRepository
    private val plantList = mutableListOf<Plant>()

    private val addEditPlantLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadPlantsFromApi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        plantRepository = PlantRepository()

        setupRecyclerView()
        setupClickListeners()

        if (isInternetAvailable()) {
            loadPlantsFromApi()
        } else {
            showNoInternetMessage()
            loadDummyData() // Load dummy data as fallback
        }
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(plantList) { plant, action ->
            when (action) {
                "hapus" -> {
                    showDeleteConfirmationDialog(plant)
                }
                "detail" -> {
                    // Handle detail action - navigate to edit mode
                    val intent = Intent(this, AddEditPlantActivity::class.java)
                    intent.putExtra("PLANT_NAME", plant.plant_name) // Pass plant name for edit
                    intent.putExtra("IS_EDIT_MODE", true)
                    addEditPlantLauncher.launch(intent)
                }
            }
        }

        binding.rvPlantList.apply {
            layoutManager = LinearLayoutManager(this@HomePageActivity)
            adapter = plantAdapter

            val dividerItemDecoration = DividerItemDecoration(
                this@HomePageActivity,
                LinearLayoutManager.VERTICAL
            )
            addItemDecoration(dividerItemDecoration)
        }
    }

    private fun setupClickListeners() {
        binding.btnTambahList.setOnClickListener {
            val intent = Intent(this, AddEditPlantActivity::class.java)
            intent.putExtra("IS_EDIT_MODE", false)
            addEditPlantLauncher.launch(intent)
        }

        binding.swipeRefreshLayout?.setOnRefreshListener {
            if (isInternetAvailable()) {
                loadPlantsFromApi()
            } else {
                showNoInternetMessage()
                binding.swipeRefreshLayout?.isRefreshing = false
            }
        }
    }

    private fun loadPlantsFromApi() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                plantRepository.getAllPlants()
                    .onSuccess { plants ->
                        runOnUiThread {
                            if (plants.isNotEmpty()) {
                                plantAdapter.updateData(plants)

                                Toast.makeText(
                                    this@HomePageActivity,
                                    "Data berhasil dimuat dari server",
                                    Toast.LENGTH_SHORT
                                ).show()

                                binding.tvEmptyState?.visibility = View.GONE
                                binding.rvPlantList.visibility = View.VISIBLE
                            } else {
                                binding.tvEmptyState?.visibility = View.VISIBLE
                                binding.rvPlantList.visibility = View.GONE

                                Toast.makeText(
                                    this@HomePageActivity,
                                    "Tidak ada data tanaman di server",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            setLoading(false)
                        }
                    }
                    .onFailure { exception ->
                        runOnUiThread {
                            handleApiError(exception)
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    handleApiError(e)
                }
            }
        }
    }

    private fun handleApiError(exception: Throwable) {
        val errorMessage = when {
            exception.message?.contains("UnknownHostException") == true ->
                "Server tidak dapat dijangkau. Pastikan koneksi internet aktif."
            exception.message?.contains("timeout") == true ->
                "Koneksi timeout. Coba lagi nanti."
            exception.message?.contains("ConnectException") == true ->
                "Gagal terhubung ke server. Server mungkin sedang maintenance."
            else -> "Gagal memuat data: ${exception.message}"
        }

        Toast.makeText(
            this@HomePageActivity,
            "$errorMessage\nMenampilkan data offline.",
            Toast.LENGTH_LONG
        ).show()

        loadDummyData()
        setLoading(false)
    }

    private fun showDeleteConfirmationDialog(plant: Plant) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Tanaman")
            .setMessage("Apakah Anda yakin ingin menghapus ${plant.plant_name}?")
            .setPositiveButton("Hapus") { _, _ ->
                deletePlant(plant)
            }
            .setNegativeButton("Batal", null)
            .setCancelable(false)
            .show()
    }

    private fun deletePlant(plant: Plant) {
        if (!isInternetAvailable()) {
            Toast.makeText(this, "Tidak ada koneksi internet untuk menghapus data", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                plantRepository.deletePlant(plant.plant_name) // Use plant_name for delete
                    .onSuccess {
                        runOnUiThread {
                            Toast.makeText(
                                this@HomePageActivity,
                                "${plant.plant_name} berhasil dihapus",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadPlantsFromApi()
                        }
                    }
                    .onFailure { exception ->
                        runOnUiThread {
                            Toast.makeText(
                                this@HomePageActivity,
                                "Gagal menghapus tanaman: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            setLoading(false)
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@HomePageActivity,
                        "Terjadi kesalahan: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }
            }
        }
    }

    private fun loadDummyData() {
        // Fallback dummy data when API fails
        val dummyPlants = listOf(
            Plant(1, "Monstera Deliciosa", "Tanaman hias daun yang populer, mudah dirawat dan cocok untuk indoor", "Rp 150.000"),
            Plant(2, "Sansevieria", "Tanaman lidah mertua yang tahan lama dan baik untuk pembersih udara", "Rp 75.000"),
            Plant(3, "Pothos", "Tanaman merambat yang cocok untuk pemula, mudah tumbuh dan adaptif", "Rp 50.000"),
            Plant(4, "Philodendron", "Tanaman tropis dengan daun yang indah, cocok untuk dekorasi rumah", "Rp 125.000"),
            Plant(5, "Rubber Plant", "Tanaman karet dengan daun mengkilap, mudah perawatan", "Rp 100.000"),
            Plant(6, "Peace Lily", "Tanaman bunga putih yang elegan, bagus untuk indoor", "Rp 80.000")
        )

        plantAdapter.updateData(dummyPlants)
        binding.tvEmptyState?.visibility = View.GONE
        binding.rvPlantList.visibility = View.VISIBLE

        Toast.makeText(this, "Menampilkan data offline", Toast.LENGTH_SHORT).show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefreshLayout?.isRefreshing = false
            btnTambahList.isEnabled = !isLoading
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun showNoInternetMessage() {
        Toast.makeText(
            this,
            "Tidak ada koneksi internet. Menampilkan data offline.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isInternetAvailable()) {
            loadPlantsFromApi()
        }
    }
}