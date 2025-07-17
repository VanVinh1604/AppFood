package com.example.foodapp.Adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.foodapp.Fragment.NotReviewedFragment
import com.example.foodapp.Fragment.ReviewedFragment


class ReviewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NotReviewedFragment()
            1 -> ReviewedFragment()
            else -> throw IllegalArgumentException("Invalid tab position: $position")
        }
    }
}
