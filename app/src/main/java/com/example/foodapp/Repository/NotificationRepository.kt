package com.example.foodapp.Repository

import com.example.foodapp.Domain.NotificationModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONObject


class NotificationRepository {
    fun getRecentNotifications(userId: String, onResult: (List<NotificationModel>) -> Unit, onError: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("Notifications").child(userId)

        ref.orderByChild("timestamp").limitToLast(10)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<NotificationModel>()

                    for (child in snapshot.children) {
                        val title = child.child("title").getValue(String::class.java)
                        val body = child.child("body").getValue(String::class.java)
                        val data = child.child("data").getValue(String::class.java)
                        val timestamp = child.child("timestamp").getValue(Long::class.java)
                        val dataJson = data?.let { JSONObject(it) }



                        val notification = NotificationModel(title, body, data, timestamp)
                        list.add(notification)
                    }
                    onResult(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }
}