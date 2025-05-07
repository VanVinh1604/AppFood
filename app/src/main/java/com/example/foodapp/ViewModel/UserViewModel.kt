package com.example.foodapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.CustomerModel
import com.example.foodapp.Repository.UserRepository

class UserViewModel: ViewModel() {
    private val userRepo = UserRepository()

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        userRepo.loginUser(email, password) { success ->
            callback(success)
        }
    }

    fun signUpCustomer(customer: CustomerModel, callback: (Boolean, String?) -> Unit) {
        userRepo.signUpCustomer(customer, callback)
    }
}