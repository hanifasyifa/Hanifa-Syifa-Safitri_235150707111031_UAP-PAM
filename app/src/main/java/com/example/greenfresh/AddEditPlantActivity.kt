package com.example.greenfresh

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.greenfresh.data.PlantRequest
import com.example.greenfresh.databinding.ActivityAddEditPlantBinding
import com.example.greenfresh.repository.PlantRepository
import kotlinx.coroutines.launch

class AddEditPlantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditPlantBinding
    private lateinit var plantRepository: PlantRepository
    private var originalPlantName: String = ""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plantRepository = PlantRepository()

        isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        originalPlantName = intent.getStringExtra("PLANT_NAME") ?: ""

        setupUI()
        setupClickListeners()

        if (isEditMode && originalPlantName.isNotEmpty()) {
            loadPlantData()
        }
    }

    private fun setupUI() {
        binding.apply {
            if (isEditMode) {
                // Update button text for edit mode
                btnSave.text = "Update Tanaman"
                supportActionBar?.title = "Edit Tanaman"
            } else {
                btnSave.text = "Simpan Tanaman"
                supportActionBar?.title = "Tambah Tanaman"
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSave.setOnClickListener {
                if (validateInput()) {
                    if (isEditMode) {
                        updatePlant()
                    } else {
                        savePlant()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun validateInput(): Boolean {
        binding.apply {
            val name = etPlantName.text.toString().trim()
            val price = etPlantPrice.text.toString().trim()
            val description = etPlantDescription.text.toString().trim()

            etPlantName.error = null
            etPlantPrice.error = null
            etPlantDescription.error = null

            if (name.isEmpty()) {
                etPlantName.error = "Nama tanaman harus diisi"
                etPlantName.requestFocus()
                return false
            }

            if (name.length < 3) {
                etPlantName.error = "Nama tanaman minimal 3 karakter"
                etPlantName.requestFocus()
                return false
            }

            if (price.isEmpty()) {
                etPlantPrice.error = "Harga harus diisi"
                etPlantPrice.requestFocus()
                return false
            }

            try {
                val cleanPrice = price.replace("Rp", "").replace(".", "").replace(",", "").trim()
                val priceValue = cleanPrice.toDouble()
                if (priceValue <= 0) {
                    etPlantPrice.error = "Harga harus lebih dari 0"
                    etPlantPrice.requestFocus()
                    return false
                }
            } catch (e: NumberFormatException) {
                etPlantPrice.error = "Format harga tidak valid"
                etPlantPrice.requestFocus()
                return false
            }

            if (description.isEmpty()) {
                etPlantDescription.error = "Deskripsi harus diisi"
                etPlantDescription.requestFocus()
                return false
            }

            if (description.length < 10) {
                etPlantDescription.error = "Deskripsi minimal 10 karakter"
                etPlantDescription.requestFocus()
                return false
            }

            return true
        }
    }

    private fun savePlant() {
        setLoading(true)

        val plantRequest = PlantRequest(
            plant_name = binding.etPlantName.text.toString().trim(),
            price = formatPrice(binding.etPlantPrice.text.toString().trim()),
            description = binding.etPlantDescription.text.toString().trim()
        )

        lifecycleScope.launch {
            try {
                plantRepository.createPlant(plantRequest)
                    .onSuccess { plant ->
                        runOnUiThread {
                            Toast.makeText(
                                this@AddEditPlantActivity,
                                "Tanaman '${plant.plant_name}' berhasil ditambahkan",
                                Toast.LENGTH_SHORT
                            ).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                    .onFailure { exception ->
                        runOnUiThread {
                            Toast.makeText(
                                this@AddEditPlantActivity,
                                "Gagal menambahkan tanaman: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            setLoading(false)
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@AddEditPlantActivity,
                        "Terjadi kesalahan: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }
            }
        }
    }

    private fun updatePlant() {
        setLoading(true)

        val plantRequest = PlantRequest(
            plant_name = binding.etPlantName.text.toString().trim(),
            price = formatPrice(binding.etPlantPrice.text.toString().trim()),
            description = binding.etPlantDescription.text.toString().trim()
        )

        lifecycleScope.launch {
            try {
                plantRepository.updatePlant(originalPlantName, plantRequest)
                    .onSuccess { plant ->
                        runOnUiThread {
                            Toast.makeText(
                                this@AddEditPlantActivity,
                                "Tanaman berhasil diupdate",
                                Toast.LENGTH_SHORT
                            ).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                    }
                    .onFailure { exception ->
                        runOnUiThread {
                            Toast.makeText(
                                this@AddEditPlantActivity,
                                "Gagal mengupdate tanaman: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            setLoading(false)
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@AddEditPlantActivity,
                        "Terjadi kesalahan: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }
            }
        }
    }

    private fun loadPlantData() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                plantRepository.getPlantByName(originalPlantName)
                    .onSuccess { plant ->
                        runOnUiThread {
                            binding.apply {
                                etPlantName.setText(plant.plant_name)
                                etPlantPrice.setText(plant.price)
                                etPlantDescription.setText(plant.description)
                            }
                            setLoading(false)
                        }
                    }
                    .onFailure { exception ->
                        runOnUiThread {
                            Toast.makeText(
                                this@AddEditPlantActivity,
                                "Gagal memuat data tanaman: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            setLoading(false)
                            finish()
                        }
                    }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@AddEditPlantActivity,
                        "Terjadi kesalahan: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                    finish()
                }
            }
        }
    }

    private fun formatPrice(price: String): String {
        val cleanPrice = price.replace("Rp", "").replace(".", "").replace(",", "").trim()
        return if (price.startsWith("Rp")) price else "Rp $cleanPrice"
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            try {
                progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                // progressBar doesn't exist in layout, ignore
            }

            btnSave.isEnabled = !isLoading
            etPlantName.isEnabled = !isLoading
            etPlantPrice.isEnabled = !isLoading
            etPlantDescription.isEnabled = !isLoading

            btnSave.text = if (isLoading) {
                if (isEditMode) "Mengupdate..." else "Menyimpan..."
            } else {
                if (isEditMode) "Update Tanaman" else "Simpan Tanaman"
            }
        }
    }
}