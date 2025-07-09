package com.example.foodapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.CustomerModel
import com.example.foodapp.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth

class UserViewModel: ViewModel() {
    private val userRepo = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        userRepo.loginUser(email, password) { success ->
            callback(success)
        }
    }
    fun signInWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        userRepo.signInWithGoogleCredential(idToken, callback)
    }


    fun signUpCustomer(customer: CustomerModel, callback: (Boolean, String?) -> Unit) {
        userRepo.signUpCustomer(customer, callback)
    }

    fun sendPasswordReset(email: String, callback: (Boolean, String?) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }
}