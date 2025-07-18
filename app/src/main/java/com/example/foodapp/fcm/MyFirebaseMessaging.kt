package com.example.foodapp.fcm


import android.R.id.message
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foodapp.Activity.MainActivity
import androidx.core.app.TaskStackBuilder
import com.example.foodapp.Activity.OrderDetailsActivity
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
        val orderId = message.data["orderId"]
        Log.d("FCM", "✅ Notification Title: $strTitle")
        Log.d("FCM", "✅ Notification Message: $strMessage")
        Log.d("FCM", "📦 orderId in data payload: $orderId")

        sendNotification(strTitle, strMessage, orderId)
    }

    private fun sendNotification(strTitle: String?, strMessage: String?, orderId: String?) {
        Log.d("FCM", "🚀 Preparing to send notification with orderId = $orderId")
        val detailIntent = Intent(this, OrderDetailsActivity::class.java).apply {
            putExtra("orderId", orderId)
        }

        val stackBuilder = TaskStackBuilder.create(this).apply {
            // Add MainActivity as parent
            addNextIntentWithParentStack(Intent(this@MyFirebaseMessaging, MainActivity::class.java))
            // Then add the OrderDetailsActivity on top
            addNextIntent(detailIntent)
        }

        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, NotificationMessages.CHANNEL_ID)
            .setContentTitle(strTitle)
            .setContentText(strMessage)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

}