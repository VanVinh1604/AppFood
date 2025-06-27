//package com.example.foodapp.Adapter
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.foodapp.Domain.ItemsModel
//import com.example.foodapp.R
//
//class FavoritesAdapter(
//    private var favoriteList: List<ItemsModel>,
//    private val onRemoveClick: (ItemsModel) -> Unit
//) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {
//
//    inner class FavoriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val itemImage: ImageView = view.findViewById(R.id.itemImage)
//        val itemName: TextView = view.findViewById(R.id.itemName)
//        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
//        val removeFavorite: ImageView = view.findViewById(R.id.removeFavorite)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorite, parent, false)
//        return FavoriteViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
//        val item = favoriteList[position]
//
//        holder.itemName.text = item.drinkName
//        holder.itemPrice.text = "$${item.drinkPrice}"
//
//        Glide.with(holder.itemView.context)
//            .load(item.drinkImage)
//            .into(holder.itemImage)
//
//        holder.removeFavorite.setOnClickListener {
//            onRemoveClick(item)
//        }
//    }
//
//    override fun getItemCount(): Int = favoriteList.size
//
//    fun updateList(newList: List<ItemsModel>) {
//        favoriteList = newList
//        notifyDataSetChanged()
//    }
//}
