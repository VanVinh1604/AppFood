package com.example.foodapp.Repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodapp.Domain.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale


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


    fun hasUnfinishedOrders(callback: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(false)

        database.child("Orders").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var hasPending = false
                    for (orderSnap in snapshot.children) {
                        val order = orderSnap.getValue(OrderDetails::class.java)
                        val status = order?.deliveryStatus?.trim()?.lowercase(Locale.getDefault()) ?: ""
                        Log.d("CheckOrderStatus", "status = $status")
                        if (status == "in_progress" || status == "shipping" || status == "unconfirmed") {
                            hasPending = true
                            break
                        }
                    }
                    callback(hasPending)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    fun fetchOrder(orderId: String, callback: (OrderDetails?) -> Unit) {
        Log.d("ViewModel", "Fetching order with ID: $orderId")
        val customerId = FirebaseAuth.getInstance().currentUser?.uid ?: return callback(null)

        val ref = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child(customerId)
            .child(orderId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedOrder = snapshot.getValue(OrderDetails::class.java)
                fetchedOrder?.itemPushKey = orderId
                callback(fetchedOrder)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }
    fun listenToDeliveryStatus(customerId: String, itemKey: String, callback: (String) -> Unit) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Orders")
            .child(customerId)
            .child(itemKey)
            .child("deliveryStatus")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java) ?: ""
                callback(status)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderStatus", "listenToDeliveryStatus error: ${error.message}")
            }
        })
    }


}
