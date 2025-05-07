package com.example.foodapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.os.Build
import androidx.appcompat.app.AppCompatActivity

class NotificationMessages : AppCompatActivity() {

    companion object {
        public const val CHANNEL_ID = "push_notification_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gọi phương thức tạo NotificationChannel
        createNotificationChannel()

    }

    // Phương thức tạo NotificationChannel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Tạo một notification channel nếu hệ điều hành Android phiên bản O (API 26) hoặc cao hơn
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Push Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            // Đăng ký channel vào NotificationManager
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    }
