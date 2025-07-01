package com.example.foodapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodapp.Domain.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrdersRepository {
    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun getOrders(): LiveData<List<OrderDetails>> {
        val orderListLiveData = MutableLiveData<List<OrderDetails>>()
        val uid = auth.currentUser?.uid ?: return orderListLiveData

        database.child("Orders").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<OrderDetails>()
                    for (orderSnap in snapshot.children) {
                        val order = orderSnap.getValue(OrderDetails::class.java)
                        order?.let {
                            it.itemPushKey = orderSnap.key // lưu key nếu cần
                            orders.add(it)
                        }
                    }
                    orderListLiveData.value = orders
                }

                override fun onCancelled(error: DatabaseError) {
                    orderListLiveData.value = emptyList()
                }
            })

        return orderListLiveData
    }
}
