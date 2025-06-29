package com.example.foodapp.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.Adapter.FavoritesAdapter
import com.example.foodapp.Domain.FavoritesModel
import com.example.foodapp.databinding.ActivityFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

}
