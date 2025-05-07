package com.example.foodapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodapp.Domain.CommentModel
import com.example.foodapp.databinding.ViewholderCommentBinding

class CommentAdapter(private var commentList: List<CommentModel>): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ViewholderCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setData(newComments: List<CommentModel>) {
        commentList = newComments
        notifyDataSetChanged()  // Cập nhật dữ liệu trong RecyclerView
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ViewholderCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val comment = commentList[position]

        holder.binding.apply {
            cusnameTxt.text = comment.customerID
            showcommentTxt.text = comment.comment
            timeTxt.text = comment.createdAt.toString()
            reviewTxt.text = comment.title
            ratingBar2.rating = comment.star

        }
    }

    override fun getItemCount(): Int = commentList.size
}
