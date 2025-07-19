package com.example.foodapp.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodapp.Domain.NotificationModel
import com.example.foodapp.Repository.NotificationRepository

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository()

    private val _notifications = MutableLiveData<List<NotificationModel>>()
    val notifications: LiveData<List<NotificationModel>> = _notifications

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchNotifications(userId: String) {
        repository.getRecentNotifications(
            userId,
            onResult = { list -> _notifications.postValue(list) },
            onError = { msg -> _error.postValue(msg) }
        )
    }
}