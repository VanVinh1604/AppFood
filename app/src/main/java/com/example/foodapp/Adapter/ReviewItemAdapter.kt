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
            drinkName.text = item.drinkName ?: "Unknown name"
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
                sendBtn.text = "Reviewed"
                sendBtn.alpha = 0.5f
            } else {
                ratingBar.isEnabled = true
                commentInput.isEnabled = true
                sendBtn.isEnabled = true
                sendBtn.text = "Submit a review"
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
            Toast.makeText(context, "You have already rated this product!", Toast.LENGTH_SHORT).show()
            return
        }

        val ratingValue = holder.binding.ratingBar.rating
        val commentText = holder.binding.commentInput.text.toString().trim()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val drinkId = item.drinkId

        if (currentUser == null) {
            Toast.makeText(context, "Please log in to submit a review", Toast.LENGTH_SHORT).show()
            return
        }

        when {
            drinkId.isNullOrEmpty() -> {
                Toast.makeText(context, "Error: Product information not found", Toast.LENGTH_SHORT).show()
                return
            }
            ratingValue == 0f -> {
                Toast.makeText(context, "Please select a rating", Toast.LENGTH_SHORT).show()
                holder.binding.ratingBar.requestFocus()
                return
            }
            commentText.isEmpty() -> {
                Toast.makeText(context, "Please enter your comment", Toast.LENGTH_SHORT).show()
                holder.binding.commentInput.requestFocus()
                return
            }
            commentText.length < 10 -> {
                Toast.makeText(context, "Comment must be at least 10 characters long", Toast.LENGTH_SHORT).show()
                holder.binding.commentInput.requestFocus()
                return
            }
        }

        holder.binding.sendBtn.apply {
            isEnabled = false
            text = "Submitting..."
        }

        // Create comment data matching the format in DetailActivity
        val commentData = hashMapOf(
            "comment" to commentText,
            "createdAt" to System.currentTimeMillis(),
            "customerID" to currentUser.uid,
            "star" to ratingValue.toInt(),
            "title" to "User Reviews"
        )

        // drinkId has been checked above, so it's safe to use !!
        val safeDrinkId = drinkId!!

        // Save to Comments node for display in DetailActivity
        val commentRef = FirebaseDatabase.getInstance()
            .getReference("Comments")
            .child(safeDrinkId)
            .push() // Generate automatic key

        commentRef.setValue(commentData)
            .addOnSuccessListener {
                // Save to Reviews node to track user review
                val reviewRef = FirebaseDatabase.getInstance()
                    .getReference("Reviews")
                    .child(safeDrinkId)
                    .child(currentUser.uid)

                reviewRef.setValue(true) // Only save true to mark as reviewed
                    .addOnSuccessListener {
                        holder.binding.apply {
                            sendBtn.text = "Reviewed"
                            sendBtn.alpha = 0.5f
                            ratingBar.isEnabled = false
                            commentInput.isEnabled = false
                        }

                        item.isReviewed = true
                        Toast.makeText(context, "Thank you for your review!", Toast.LENGTH_SHORT).show()

                        reviewListener?.onReviewSubmitted(position)

                        // Update average rating
                        updateProductAverageRating(safeDrinkId)
                    }
            }
            .addOnFailureListener { e ->
                holder.binding.sendBtn.apply {
                    isEnabled = true
                    text = "Submit a review"
                }

                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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

                    // Update rating in Popular node
                    FirebaseDatabase.getInstance()
                        .getReference("Popular")
                        .child(drinkId)
                        .child("rating")
                        .setValue(averageRating)

                    // Update rating in Items node (if exists)
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