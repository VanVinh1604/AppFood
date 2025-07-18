package com.example.foodapp.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.Adapter.ReviewPagerAdapter
import com.example.foodapp.databinding.ActivityReviewBinding
import com.google.android.material.tabs.TabLayoutMediator

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private val tabTitles = arrayOf("Chưa đánh giá", "Đã đánh giá")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gắn adapter
        val adapter = ReviewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Gắn tab layout
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
