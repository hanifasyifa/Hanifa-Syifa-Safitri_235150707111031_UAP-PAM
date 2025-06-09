package com.example.greenfresh.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenfresh.R
import com.example.greenfresh.data.Plant
import com.example.greenfresh.databinding.ItemPlantBinding

class PlantAdapter(
    private val plantList: MutableList<Plant>,
    private val onItemClick: (Plant, String) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    inner class PlantViewHolder(private val binding: ItemPlantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plant: Plant) {
            binding.apply {
                // Use plant_name from API response
                tvPlantName.text = plant.plant_name
                tvPlantPrice.text = plant.price

                // Load image using Glide if imageUrl is available, otherwise use default
                // if (!plant.imageUrl.isNullOrEmpty()) {
                //     Glide.with(itemView.context)
                //         .load(plant.imageUrl)
                //         .placeholder(R.drawable.plant_sample)
                //         .error(R.drawable.plant_sample)
                //         .into(ivPlantImage)
                // } else {
                //     ivPlantImage.setImageResource(R.drawable.plant_sample)
                // }

                btnHapus.setOnClickListener {
                    onItemClick(plant, "hapus")
                }

                btnDetail.setOnClickListener {
                    onItemClick(plant, "detail")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(plantList[position])
    }

    override fun getItemCount(): Int = plantList.size

    // Method to update data
    fun updateData(newPlantList: List<Plant>) {
        plantList.clear()
        plantList.addAll(newPlantList)
        notifyDataSetChanged()
    }

    // Method to remove item
    fun removeItem(position: Int) {
        if (position >= 0 && position < plantList.size) {
            plantList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, plantList.size)
        }
    }
}