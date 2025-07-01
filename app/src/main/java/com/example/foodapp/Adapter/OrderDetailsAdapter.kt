package com.example.foodapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.databinding.ViewholderOrderDrinkBinding

class OrderDetailsAdapter (
    private val drinkNames: List<String>,
    private val drinkImages: List<String>,
    private val drinkPrices: List<String>,
    private val drinkQuantities: List<Int>,
    private val drinkSizes: List<String>
) : RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ViewholderOrderDrinkBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderOrderDrinkBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = drinkNames.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding) {
            textDrinkName.text = drinkNames[position]
            textDrinkPrice.text = "Giá: ${drinkPrices.getOrNull(position) ?: "0"} đ"
            textDrinkQuantity.text = "Số lượng: ${drinkQuantities.getOrNull(position) ?: 1}"

            val size = drinkSizes.getOrNull(position)
            textDrinkSize.text = if (!size.isNullOrBlank()) "Size: $size" else "Size: -"

            Glide.with(imageDrink.context)
                .load(drinkImages.getOrNull(position))
                .into(imageDrink)
        }
    }

}