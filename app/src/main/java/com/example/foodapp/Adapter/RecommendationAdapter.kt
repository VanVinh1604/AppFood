package com.example.foodapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.Activity.DetailActivity
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.example.foodapp.databinding.ItemRecommendationBinding

class RecommendationAdapter(
    private val context: Context,
    private var recommendationList: List<ItemsModel>
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemRecommendationBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = recommendationList[position]
        with(holder.binding) {
            recommendationName.text = item.drinkName ?: "Tên không có"
            recommendationPrice.text = "$${item.drinkPrice ?: 0.0}"
            Glide.with(context)
                .load(item.drinkImage?.takeIf { it.isNotEmpty() } ?: R.drawable.ic_menu_gallery)
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_menu_gallery)
                .into(recommendationImage)

            root.setOnClickListener {
                val intent = Intent(context, DetailActivity::class.java)
                intent.putExtra("object", item)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = recommendationList.size

    fun setData(newList: List<ItemsModel>) {
        recommendationList = newList
        notifyDataSetChanged()
    }
}