package com.example.foodapp.Activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Typeface

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodapp.Adapter.CategoryAdapter
import com.example.foodapp.Adapter.PopularAdapter
import com.example.foodapp.Domain.ItemsModel
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityMainBinding
import com.example.foodapp.utils.dp
import com.example.foodapp.FloatingChatService
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel = MainViewModel()
    private lateinit var popularAdapter: PopularAdapter
//    private var categoryList = listOf<CategoryModel>()  // Lưu data để search
    private var popularList = listOf<ItemsModel>()    // Lưu data để search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kiểm tra quyền overlay để hiển thị icon chat nổi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 1234)
        } else {
            startService(Intent(this, FloatingChatService::class.java))
        }

        initBanner()
        initCategory()
        initPopular()
        initBottomMenu()
        initFirebaseMessaging()
        initSearchButton()


        // initFirebaseMessaging() nếu bạn muốn dùng thông báo FCM sau này
    }

    // Khi user quay lại từ màn hình cấp quyền overlay
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234 && Settings.canDrawOverlays(this)) {
            startService(Intent(this, FloatingChatService::class.java))
        } else {
            Toast.makeText(this, "Bạn chưa cấp quyền hiển thị icon chat", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initSearchButton() {
        binding.searchBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, ItemsListActivity::class.java)
            // Trong MainActivity
            intent.putExtra("categoryId", "all")
            intent.putExtra("category_Name", "All Products")

            startActivity(intent)
        }
    }

    private fun initBottomMenu() {
        val rootView = findViewById<View>(R.id.bottomMenuInclude)

        val homeIcon = rootView.findViewById<ImageView>(R.id.imageHome)
        val homeText = rootView.findViewById<TextView>(R.id.textHome)

        // Đặt màu vàng + phóng to khi ở Home
        homeIcon.setColorFilter(getColor(R.color.orange)) // bạn cần thêm màu vào colors.xml
        homeIcon.layoutParams.width = 28.dp  // bạn có thể tạo hàm chuyển đổi dp
        homeIcon.layoutParams.height = 28.dp
        homeIcon.requestLayout()

        homeText.setTextColor(getColor(R.color.orange))
        homeText.setTypeface(null, Typeface.BOLD)


        val cartBtn = rootView.findViewById<View>(R.id.cartBtn)
        cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))

        }

        // FAVORITE
        val favoriteBtn = rootView.findViewById<View>(R.id.favoriteBtn)
        favoriteBtn.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // ORDER
        val orderBtn = rootView.findViewById<View>(R.id.orderBtn)
        orderBtn.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
        }

        // PROFILE
        val profileBtn = rootView.findViewById<View>(R.id.profileBtn)
        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }

    private fun initBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE
        viewModel.loadBanner().observeForever {
            Glide.with(this@MainActivity)
                .load(it[0].url)
                .into(binding.banner)
            binding.progressBarBanner.visibility = View.GONE
        }
        viewModel.loadBanner()
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observe(this) { list ->
            binding.recyclerViewCat.layoutManager = LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false
            )
            binding.recyclerViewCat.adapter = CategoryAdapter(list.toMutableList()) { cat ->
                startActivity(Intent(this, ItemsListActivity::class.java).apply {
                    putExtra("categoryId", cat.categoryId ?: "")
                    putExtra("category_Name", cat.category_Name ?: "")
                })
            }
            binding.progressBarCategory.visibility = View.GONE
        }
    }
    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this) { populars ->
            populars?.let {
                popularList = it
                popularAdapter = PopularAdapter(it.toMutableList())
                binding.recyclerViewPopular.layoutManager = GridLayoutManager(this, 2)
                binding.recyclerViewPopular.adapter = popularAdapter
            }
            binding.progressBarPopular.visibility = View.GONE
        }
    }


    private fun initFirebaseMessaging() {
        // Tạo NotificationChannel cho Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "push_notification_id",
                "Push Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Lấy FCM Token để test gửi từ Firebase Console
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Lấy token thất bại", task.exception)
                return@addOnCompleteListener
            }

            // Token lấy thành công
            val token = task.result
            Log.d("FCM_TOKEN", "Token: $token")
            Toast.makeText(this, "FCM Token đã sẵn sàng", Toast.LENGTH_SHORT).show()

            // TODO: Gửi token này lên server nếu muốn
        }
    }


}

