package com.example.foodapp.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding:ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        // Nếu đã đăng nhập → chuyển thẳng đến MainActivity (hoặc OrderDetailsActivity nếu cần)
        if (isLoggedIn) {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish() // Đóng SplashActivity để không quay lại khi bấm Back
            return
        }

        // Nếu chưa đăng nhập → chờ người dùng bấm nút để login
        binding.startBtn.setOnClickListener {
            startActivity(Intent(this, UserLoginActivity::class.java))
        }
    }
}