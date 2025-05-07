package com.example.foodapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodapp.Domain.CommentModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadComment(drinkId: String): LiveData<MutableList<CommentModel>> {
        val commentLiveData = MutableLiveData<MutableList<CommentModel>>()
        val ref = firebaseDatabase.getReference("Comments").orderByChild("drinkId").equalTo(drinkId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val commentList = mutableListOf<CommentModel>()
                for (child in snapshot.children) {
                    val comment = child.getValue(CommentModel::class.java)
                    comment?.let { commentList.add(it) }
                }
                commentLiveData.value = commentList
            }

            override fun onCancelled(error: DatabaseError) {
                // Xử lý khi có lỗi
            }
        })

        return commentLiveData
    }
}