package com.example.foodapp.Repository

import android.util.Log
import com.example.foodapp.Domain.CustomerModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
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
            callback(false, "Email or password cannot be blank")
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
                                Log.d("SignUp", "Data recording successful")
                                callback(true, null)
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignUp", "Data write error: ${e.message}")
                                callback(false, e.message)
                            }
                    } else {
                        callback(false, "Unable to get user UID")
                    }
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Log.e("SignUp", "Registration error: $errorMsg")
                    callback(false, errorMsg)
                }
            }
    }
    fun signInWithGoogleCredential(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val customer = CustomerModel(
                        nameCustomer = user?.displayName,
                        emailCustomer = user?.email,
                    //    profileImage = user?.photoUrl?.toString()
                    )
                    val uid = user?.uid
                    if (uid != null) {
                        val dbRef = firebaseDatabase.getReference("Users").child(uid)
                        dbRef.setValue(customer)
                            .addOnSuccessListener { callback(true, null) }
                            .addOnFailureListener { callback(false, it.message) }
                    } else {
                        callback(false, "User UID not found")
                    }
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }



}