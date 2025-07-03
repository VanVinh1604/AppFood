package com.example.foodapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.Activity.DetailActivity
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FavoritesAdapter(
    private val context: Context,
    private val list: MutableList<ItemsModel>
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgDrink: ImageView = view.findViewById(R.id.imgDrink)
        val tvDrinkName: TextView = view.findViewById(R.id.tvDrinkName)
        val tvDrinkPrice: TextView = view.findViewById(R.id.tvDrinkPrice)
        val btnRemoveFavorite: ImageView = view.findViewById(R.id.btnRemoveFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.viewholder_favorites, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        val drinkId = item.drinkId ?: ""
        val drinkName = item.drinkName ?: ""
        val drinkImage = item.drinkImage ?: ""
        val drinkPrice = item.drinkPrice ?: 0.0

        if (drinkId.isEmpty() || drinkName.isEmpty() || drinkImage.isEmpty()) {
            Toast.makeText(context, "Dữ liệu sản phẩm không đầy đủ", Toast.LENGTH_SHORT).show()
            return
        }

        holder.tvDrinkName.text = drinkName
        holder.tvDrinkPrice.text = "$drinkPrice$"

        Glide.with(context)
            .load(drinkImage)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(holder.imgDrink)

        holder.imgDrink.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("object", item)
            context.startActivity(intent)
        }

        holder.btnRemoveFavorite.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
            FirebaseDatabase.getInstance().getReference("Favorites")
                .child(userId).child(drinkId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ItemsModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
