package com.example.foodapp.Adapter

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Activity.ItemsListActivity
import com.example.foodapp.Domain.CategoryModel
import com.example.foodapp.R
import com.example.foodapp.databinding.ViewholderCategoryBinding // ✅ dùng đúng binding class

class CategoryAdapter(val items: MutableList<CategoryModel>)
    : RecyclerView.Adapter<CategoryAdapter.Viewholder>() {

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    inner class Viewholder(val binding: ViewholderCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }


    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item=items[position]
        holder.binding.titleCat.text=item.category_Name
        holder.binding.root.setOnClickListener{
            lastSelectedPosition=selectedPosition
            selectedPosition=position
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(selectedPosition)

            Handler(Looper.getMainLooper()).postDelayed({

                val intent= Intent(context, ItemsListActivity::class.java).apply {

                    putExtra("categoryId", item.categoryId.toString())

                    putExtra("category_Name", item.category_Name)

                }

                ContextCompat.startActivity(context, intent,  null)

            }, 500)
        }
        if(selectedPosition==position){
            holder.binding.titleCat.setBackgroundResource(R.drawable.dark_brown_bg)
            holder.binding.titleCat.setTextColor(context.resources.getColor(R.color.white))
        }else{
            holder.binding.titleCat.setBackgroundResource(R.drawable.white_bg)
            holder.binding.titleCat.setTextColor(context.resources.getColor(R.color.darkBrown))
        }
    }

    override fun getItemCount(): Int = items.size

//    fun updateData(newItems: List<CategoryModel>) {
//        items.clear()
//        items.addAll(newItems)
//        notifyDataSetChanged()
//    }
}