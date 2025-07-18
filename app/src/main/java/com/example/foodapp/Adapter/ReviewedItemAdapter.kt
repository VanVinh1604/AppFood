package com.example.foodapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.Domain.OrderItem
import com.example.foodapp.databinding.ItemReviewedBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReviewedItemAdapter(
    private val context: Context,
    private val itemList: List<OrderItem>,
    private val reviewComments: Map<String, Pair<String, Int>> // drinkId -> (comment, rating)
) : RecyclerView.Adapter<ReviewedItemAdapter.ReviewedViewHolder>() {

    inner class ReviewedViewHolder(val binding: ItemReviewedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewedViewHolder {
        val binding = ItemReviewedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewedViewHolder, position: Int) {
        val item = itemList[position]
        // Extract real drinkId và commentId từ format "drinkId-commentId"
        val idParts = item.drinkId?.split("-") ?: listOf("")
        val realDrinkId = idParts[0]
        val reviewData = reviewComments[item.drinkId]

        holder.binding.apply {
            // Set thông tin sản phẩm
            drinkName.text = item.drinkName ?: "Không rõ tên"
            drinkSize.text = "Size: ${item.drinkSize ?: "M"}"

            // Format giá
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            drinkPrice.text = formatter.format(item.drinkPrice ?: 0.0)

            // Load hình ảnh
            Glide.with(context)
                .load(item.drinkImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .centerCrop()
                .into(drinkImage)

            // Hiển thị review data nếu có
            reviewData?.let { (comment, rating) ->
                ratingBar.rating = rating.toFloat()
                ratingBar.isEnabled = false
                userComment.text = comment
            }

            // Format và hiển thị ngày review (dùng orderTime là thời gian review)
            if (item.orderTime > 0) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                reviewDate.text = "Đã đánh giá: ${dateFormat.format(Date(item.orderTime))}"
            }
        }
    }

    override fun getItemCount(): Int = itemList.size
}