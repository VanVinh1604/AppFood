package com.example.foodapp.Repository

import android.util.Log
import com.example.foodapp.Domain.CustomerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()


    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }
    // MainRepository.kt
    fun signUpCustomer(customer: CustomerModel, callback: (Boolean, String?) -> Unit) {
        val auth = FirebaseAuth.getInstance()

        val email = customer.emailCustomer
        val password = customer.passwordCustomer

        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            callback(false, "Email hoặc mật khẩu không được để trống")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val dbRef = FirebaseDatabase.getInstance().getReference("Customers").child(uid)
                        dbRef.setValue(customer)
                            .addOnSuccessListener {
                                Log.d("SignUp", "Ghi dữ liệu thành công")
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignUp", "Lỗi ghi dữ liệu: ${e.message}")
                                callback(false, e.message)
                            }
                    } else {
                        callback(false, "Không lấy được UID người dùng")
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Lỗi không xác định"
                    Log.e("SignUp", "Lỗi đăng ký: $errorMsg")
                    callback(false, errorMsg)
                }
            }
    }


}