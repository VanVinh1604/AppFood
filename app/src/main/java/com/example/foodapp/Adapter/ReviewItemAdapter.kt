package com.example.foodapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodapp.Domain.OrderItem
import com.example.foodapp.databinding.ItemReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ReviewItemAdapter(
    private val context: Context,
    private val itemList: List<OrderItem>
) : RecyclerView.Adapter<ReviewItemAdapter.ReviewViewHolder>() {

    interface OnReviewSubmittedListener {
        fun onReviewSubmitted(position: Int)
    }

    private var reviewListener: OnReviewSubmittedListener? = null

    fun setOnReviewSubmittedListener(listener: OnReviewSubmittedListener) {
        this.reviewListener = listener
    }

    inner class ReviewViewHolder(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = itemList[position]

        holder.binding.apply {
            drinkName.text = item.drinkName ?: "Không rõ tên"
            drinkSize.text = "Size: ${item.drinkSize ?: "M"}"

            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            drinkPrice.text = formatter.format(item.drinkPrice ?: 0.0)

            Glide.with(context)
                .load(item.drinkImage)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_dialog_alert)
                .centerCrop()
                .into(drinkImage)

            if (item.isReviewed) {
                ratingBar.isEnabled = false
                commentInput.isEnabled = false
                sendBtn.isEnabled = false
                sendBtn.text = "Đã đánh giá"
                sendBtn.alpha = 0.5f
            } else {
                ratingBar.isEnabled = true
                commentInput.isEnabled = true
                sendBtn.isEnabled = true
                sendBtn.text = "Gửi đánh giá"
                sendBtn.alpha = 1.0f
                ratingBar.rating = 0f
                commentInput.setText("")
            }

            sendBtn.setOnClickListener {
                handleReviewSubmit(item, holder, position)
            }
        }
    }

    private fun handleReviewSubmit(item: OrderItem, holder: ReviewViewHolder, position: Int) {
        if (item.isReviewed) {
            Toast.makeText(context, "Bạn đã đánh giá sản phẩm này!", Toast.LENGTH_SHORT).show()
            return
        }

        val ratingValue = holder.binding.ratingBar.rating
        val commentText = holder.binding.commentInput.text.toString().trim()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val drinkId = item.drinkId

        if (currentUser == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show()
            return
        }

        when {
            drinkId.isNullOrEmpty() -> {
                Toast.makeText(context, "Lỗi: Không tìm thấy thông tin sản phẩm", Toast.LENGTH_SHORT).show()
                return
            }
            ratingValue == 0f -> {
                Toast.makeText(context, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show()
                holder.binding.ratingBar.requestFocus()
                return
            }
            commentText.isEmpty() -> {
                Toast.makeText(context, "Vui lòng nhập nhận xét của bạn", Toast.LENGTH_SHORT).show()
                holder.binding.commentInput.requestFocus()
                return
            }
            commentText.length < 10 -> {
                Toast.makeText(context, "Nhận xét phải có ít nhất 10 ký tự", Toast.LENGTH_SHORT).show()
                holder.binding.commentInput.requestFocus()
                return
            }
        }

        holder.binding.sendBtn.apply {
            isEnabled = false
            text = "Đang gửi..."
        }

        // Tạo comment data giống format trong DetailActivity
        val commentData = hashMapOf(
            "comment" to commentText,
            "createdAt" to System.currentTimeMillis(),
            "customerID" to currentUser.uid,
            "star" to ratingValue.toInt(),
            "title" to "User Reviews"
        )

        // drinkId đã được check ở trên nên safe để dùng !!
        val safeDrinkId = drinkId!!

        // Lưu vào Comments node để hiển thị trong DetailActivity
        val commentRef = FirebaseDatabase.getInstance()
            .getReference("Comments")
            .child(safeDrinkId)
            .push() // Tạo key tự động

        commentRef.setValue(commentData)
            .addOnSuccessListener {
                // Lưu thêm vào Reviews để track user đã review
                val reviewRef = FirebaseDatabase.getInstance()
                    .getReference("Reviews")
                    .child(safeDrinkId)
                    .child(currentUser.uid)

                reviewRef.setValue(true) // Chỉ lưu true để đánh dấu đã review
                    .addOnSuccessListener {
                        holder.binding.apply {
                            sendBtn.text = "Đã đánh giá"
                            sendBtn.alpha = 0.5f
                            ratingBar.isEnabled = false
                            commentInput.isEnabled = false
                        }

                        item.isReviewed = true
                        Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()

                        reviewListener?.onReviewSubmitted(position)

                        // Update rating trung bình
                        updateProductAverageRating(safeDrinkId)
                    }
            }
            .addOnFailureListener { e ->
                holder.binding.sendBtn.apply {
                    isEnabled = true
                    text = "Gửi đánh giá"
                }

                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProductAverageRating(drinkId: String) {
        val commentsRef = FirebaseDatabase.getInstance()
            .getReference("Comments")
            .child(drinkId)

        commentsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                var totalRating = 0.0
                var reviewCount = 0

                snapshot.children.forEach { commentSnapshot ->
                    val star = commentSnapshot.child("star").getValue(Int::class.java) ?: 0
                    totalRating += star
                    reviewCount++
                }

                if (reviewCount > 0) {
                    val averageRating = totalRating / reviewCount

                    // Update rating trong Popular node
                    FirebaseDatabase.getInstance()
                        .getReference("Popular")
                        .child(drinkId)
                        .child("rating")
                        .setValue(averageRating)

                    // Update rating trong Items node (nếu có)
                    FirebaseDatabase.getInstance()
                        .getReference("Items")
                        .child(drinkId)
                        .child("rating")
                        .setValue(averageRating)
                }
            }
        }
    }

    override fun getItemCount(): Int = itemList.size
}