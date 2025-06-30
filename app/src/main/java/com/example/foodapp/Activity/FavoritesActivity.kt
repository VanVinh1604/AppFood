package com.example.foodapp.Activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Adapter.FavoritesAdapter
import com.example.foodapp.Domain.FavoritesModel
import com.example.foodapp.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.foodapp.R
import com.example.foodapp.utils.dp

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavoritesAdapter
    private lateinit var database: DatabaseReference
    private val favoritesList = mutableListOf<FavoritesModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = FavoritesAdapter(this, favoritesList)
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFavorites.adapter = adapter

        fetchFavorites()
        binding.backBtn.setOnClickListener {
            finish() // Đóng FavoritesActivity và quay lại MainActivity
        }

        setupBottomMenu()
    }


    private fun fetchFavorites() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance().getReference("Favorites").child(userId)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<FavoritesModel>()
                for (itemSnapshot in snapshot.children) {
                    val favorite = itemSnapshot.getValue(FavoritesModel::class.java)
                    if (favorite != null && favorite.drinkId.isNotEmpty()) {
                        tempList.add(favorite)
                    }
                }
                adapter.updateData(tempList)
                binding.emptyText.visibility = if (tempList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }

        })
    }

    private fun setupBottomMenu() {
        val rootView = findViewById<View>(R.id.bottomMenuInclude)

        // FAVORITE (active)
        val favoriteIcon = rootView.findViewById<ImageView>(R.id.imagelove)
        val favoriteText = rootView.findViewById<TextView>(R.id.textlove)
        favoriteIcon.setColorFilter(getColor(R.color.orange))
        favoriteIcon.layoutParams.width = 28.dp
        favoriteIcon.layoutParams.height = 28.dp
        favoriteIcon.requestLayout()
        favoriteText.setTextColor(getColor(R.color.orange))
        favoriteText.setTypeface(null, Typeface.BOLD)

        // HOME
        val homeBtn = rootView.findViewById<View>(R.id.HomeBtn)
        homeBtn.setOnClickListener {
            val intent = Intent(this@FavoritesActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        // CART
        val cartBtn = rootView.findViewById<View>(R.id.cartBtn)
        cartBtn.setOnClickListener {
            val intent = Intent(this@FavoritesActivity, CartActivity::class.java)
            startActivity(intent)
            finish()
        }

        // ORDER
        val orderBtn = rootView.findViewById<View>(R.id.orderBtn)
        orderBtn.setOnClickListener {
            val intent = Intent(this@FavoritesActivity, OrdersActivity::class.java)
            startActivity(intent)
            finish()
        }

        // PROFILE
        val profileBtn = rootView.findViewById<View>(R.id.profileBtn)
        profileBtn.setOnClickListener {
            val intent = Intent(this@FavoritesActivity, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
