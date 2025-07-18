package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.foodapp.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.foodapp.FloatingChatService
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var profileName: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileImage: ImageView
    private lateinit var dateTimeText: TextView
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: Button
    private lateinit var changeInfo: MaterialCardView
    private lateinit var reviewSection: MaterialCardView
    private lateinit var languageChange: MaterialCardView
    private lateinit var aboutApp: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupProfileInfo()
        setupClickEvents()
        setupBottomMenu()
        updateDateTime()
    }

    private fun initViews() {
        profileName = findViewById(R.id.profile_name)
        profileUsername = findViewById(R.id.profile_username)
        profileImage = findViewById(R.id.profile_image)
        dateTimeText = findViewById(R.id.date_time_text)
        backButton = findViewById(R.id.back_button)
        logoutButton = findViewById(R.id.logout_button)

        changeInfo = findViewById(R.id.change_info)
        reviewSection = findViewById(R.id.review_section)
        languageChange = findViewById(R.id.language_change)
        aboutApp = findViewById(R.id.about_app)

        auth = FirebaseAuth.getInstance()
    }

    private fun updateDateTime() {
        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(currentTime)
        dateTimeText.text = "Today is $formattedDate"
    }

    private fun setupProfileInfo() {
        try {
            val userId = auth.currentUser?.uid ?: return

            databaseRef = FirebaseDatabase.getInstance().getReference("Customers").child(userId)

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("nameCustomer").getValue(String::class.java)
                    val email = snapshot.child("emailCustomer").getValue(String::class.java)
                    val avatarUrl = snapshot.child("avatarUrl").getValue(String::class.java)

                    profileName.text = name ?: "No name"
                    profileUsername.text = email ?: ""

                    if (!avatarUrl.isNullOrEmpty()) {
                        Glide.with(this@ProfileActivity)
                            .load(avatarUrl)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .circleCrop()
                            .into(profileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun setupClickEvents() {
        backButton.setOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
            // Dừng bong bóng chat
            stopService(Intent(this, FloatingChatService::class.java))

            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
            sharedPref.edit().remove("isLoggedIn").apply()
            auth.signOut()

            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UserLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        changeInfo.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        reviewSection.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            startActivity(intent)
        }

        languageChange.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        aboutApp.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun setupBottomMenu() {
        val rootView = findViewById<View>(R.id.bottomMenuInclude)

        val profileIcon = rootView.findViewById<ImageView>(R.id.imageprofile)
        val profileText = rootView.findViewById<TextView>(R.id.textprofile)
        profileIcon.setColorFilter(getColor(R.color.orange))
        profileText.setTextColor(getColor(R.color.orange))
        profileText.setTypeface(null, android.graphics.Typeface.BOLD)

        rootView.findViewById<View>(R.id.HomeBtn).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        rootView.findViewById<View>(R.id.cartBtn).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
            finish()
        }

        rootView.findViewById<View>(R.id.favoriteBtn).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
            finish()
        }

        rootView.findViewById<View>(R.id.orderBtn).setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java))
            finish()
        }
    }
}