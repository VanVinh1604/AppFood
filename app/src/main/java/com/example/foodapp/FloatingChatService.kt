package com.example.foodapp

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView

class FloatingChatService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var chatHeadView: View
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        chatHeadView = inflater.inflate(R.layout.layout_chat_head, null)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.END
        layoutParams.x = 0
        layoutParams.y = 200

        val chatIcon = chatHeadView.findViewById<ImageView>(R.id.chat_icon)
        chatIcon.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX - (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(chatHeadView, layoutParams)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (Math.abs(event.rawX - initialTouchX) < 10 && Math.abs(event.rawY - initialTouchY) < 10) {
                        openMessengerChat()
                    }
                    true
                }

                else -> false
            }
        }

        windowManager.addView(chatHeadView, layoutParams)
    }

    private fun openMessengerChat() {
        val pageId = "598649500006063" // 👈 Thay bằng ID trang Facebook của bạn
        val messengerIntent = Intent(Intent.ACTION_VIEW)

        try {
            messengerIntent.data = Uri.parse("fb-messenger://user/$pageId")
            messengerIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(messengerIntent)
        } catch (e: Exception) {
            messengerIntent.data = Uri.parse("https://m.me/$pageId")
            messengerIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(messengerIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::chatHeadView.isInitialized) {
            windowManager.removeView(chatHeadView)
        }
    }
}
