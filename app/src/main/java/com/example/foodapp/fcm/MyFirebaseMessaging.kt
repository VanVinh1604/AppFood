package com.example.foodapp.fcm


import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foodapp.Activity.MainActivity
import com.example.foodapp.NotificationMessages
import com.example.foodapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessaging : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Gửi token lên server của bạn hoặc lưu lại nếu cần thiết
        // Ví dụ: Log token ra Logcat để kiểm tra
        Log.d("FCM_TOKEN", "New token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Message received: ${message.data}")

        val notification = message.notification
        if (notification == null) {
            Log.d("FCM", "Notification is null")
            return
        }

        val strTitle = notification.title
        val strMessage = notification.body
        Log.d("FCM", "Title: $strTitle - Message: $strMessage")

        sendNotification(strTitle, strMessage)
    }

    private fun sendNotification(strTitle: String?, strMessage: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // nên thêm FLAG_IMMUTABLE nếu dùng targetSdk >= 31
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificationMessages.CHANNEL_ID)
            .setContentTitle(strTitle)
            .setContentText(strMessage)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notification = notificationBuilder.build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.notify(1, notification)
    }
}