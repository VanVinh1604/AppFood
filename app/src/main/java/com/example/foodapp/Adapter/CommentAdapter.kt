package com.example.foodapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Domain.CommentModel
import com.example.foodapp.databinding.ViewholderCommentBinding
import com.google.firebase.database.FirebaseDatabase

class CommentAdapter(private var commentList: List<CommentModel>): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setData(newComments: List<CommentModel>) {
        commentList = newComments
        Log.d("CommentAdapter", "New comment list size: ${commentList.size}")
        notifyDataSetChanged()  // Cập nhật dữ liệu trong RecyclerView
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        Log.d("CommentAdapter", "Creating ViewHolder")
        val binding =
            ViewholderCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val comment = commentList[position]
        Log.d("CommentAdapter", "Item $position: $comment")

        holder.binding.showcommentTxt.text = comment.comment
        holder.binding.ratingBar2.rating = comment.star
        holder.binding.timeTxt.text = convertTimestamp(comment.createdAt) // chuyển thời gian nếu muốn
        holder.binding.reviewTxt.text = getReviewTextFromStar(comment.star)
        val userId = comment.customerID
        if (!userId.isNullOrEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("Customers").child(userId).child("nameCustomer")
            userRef.get().addOnSuccessListener { snapshot ->
                val name = snapshot.value?.toString() ?: "Name not found"
                holder.binding.cusnameTxt.text = name
            }.addOnFailureListener {
                holder.binding.cusnameTxt.text = "Name not found"
            }
        } else {
            holder.binding.cusnameTxt.text = "Anonymous"
        }
    }

    private fun getReviewTextFromStar(star: Float): String {
        return when {
            star >= 5 -> "Very Good"
            star >= 4 -> "Good"
            star >= 3 -> "Normal"
            star >= 2 -> "Bad"
            else -> "Very Bad"
        }
    }

    private fun convertTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp)
        return sdf.format(date)
    }

    override fun getItemCount(): Int = commentList.size
}
