package com.example.greenfresh.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
                tvPlantName.text = plant.plant_name
                tvPlantPrice.text = plant.price

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

    // update data
    fun updateData(newPlantList: List<Plant>) {
        plantList.clear()
        plantList.addAll(newPlantList)
        notifyDataSetChanged()
    }

    // remove item
    fun removeItem(position: Int) {
        if (position >= 0 && position < plantList.size) {
            plantList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, plantList.size)
        }
    }
}