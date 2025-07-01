package com.example.foodapp.Repository

import com.example.foodapp.Domain.VouchersModel
import com.google.firebase.database.*

class VoucherRepository {
    private val dbRef = FirebaseDatabase.getInstance().getReference("Vouchers")

    fun checkVoucher(
        code: String,
        onResult: (VouchersModel?, String?) -> Unit
    ) {
        dbRef.get().addOnSuccessListener { snapshot ->
            for (voucherSnap in snapshot.children) {
                val voucher = voucherSnap.getValue(VouchersModel::class.java)
                if (voucher?.code == code && voucher.active) {
                    onResult(voucher, null)
                    return@addOnSuccessListener
                }
            }
            onResult(null, "Invalid or expired coupon code.")
        }.addOnFailureListener {
            onResult(null, "Data connection error.")
        }
    }
    fun checkIfUserUsedVoucher(
        code: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        val usageRef = FirebaseDatabase.getInstance()
            .getReference("VoucherUsages")
            .child(code)
            .child(userId)

        usageRef.get().addOnSuccessListener { snapshot ->
            onResult(snapshot.exists())
        }.addOnFailureListener {
            onResult(false)
        }
    }

    fun markVoucherUsed(code: String, userId: String) {
        val usageRef = FirebaseDatabase.getInstance()
            .getReference("VoucherUsages")
            .child(code)
            .child(userId)
        usageRef.setValue(true)
    }

}
