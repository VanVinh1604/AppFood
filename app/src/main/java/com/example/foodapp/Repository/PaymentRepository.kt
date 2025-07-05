package com.example.foodapp.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodapp.Domain.CustomerModel
import com.example.foodapp.Domain.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PaymentRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Orders")

    fun fetchUserInfo(): LiveData<CustomerModel> {
        val userLiveData = MutableLiveData<CustomerModel>()
        val userId = auth.currentUser?.uid ?: return userLiveData

        val googleRef = database.getReference("GoogleUser").child(userId)
        googleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(CustomerModel::class.java)
                    userLiveData.postValue(user)
                } else {
                    val customerRef = database.getReference("Customers").child(userId)
                    customerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(CustomerModel::class.java)
                            userLiveData.postValue(user)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return userLiveData
    }

    fun decreaseVoucherUsage(code: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("Vouchers")
            .orderByChild("code")
            .equalTo(code)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (voucherSnap in snapshot.children) {
                    val current = voucherSnap.child("usageLimit").getValue(Int::class.java) ?: 0
                    if (current > 0) {
                        voucherSnap.ref.child("usageLimit").setValue(current - 1)
                    } else {
                        voucherSnap.ref.child("active").setValue(false)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun saveOrder(order: OrderDetails, onResult: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userOrderRef = database.getReference("Orders").child(userId).push()
            val pushKey = userOrderRef.key
            if (pushKey != null) {
                order.itemPushKey = pushKey
                userOrderRef.setValue(order)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } else {
                onResult(false)
            }
        }
    }
}