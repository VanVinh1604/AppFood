package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var profileName: TextView
    private lateinit var profileUsername: TextView
    private lateinit var profileImage: ImageView
    private lateinit var backButton: ImageView
    private lateinit var logoutButton: Button
    private lateinit var changeInfo: LinearLayout
    private lateinit var orderNotification: LinearLayout
    private lateinit var languageChange: LinearLayout
    private lateinit var aboutApp: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupProfileInfo()
        setupClickEvents()
        setupBottomMenu()
    }

    private fun initViews() {
        profileName = findViewById(R.id.profile_name)
        profileUsername = findViewById(R.id.profile_username)
        profileImage = findViewById(R.id.profile_image)
        backButton = findViewById(R.id.back_button)
        logoutButton = findViewById(R.id.logout_button)

        changeInfo = findViewById(R.id.change_info)
        orderNotification = findViewById(R.id.order_notification)
        languageChange = findViewById(R.id.language_change)
        aboutApp = findViewById(R.id.about_app)

        auth = FirebaseAuth.getInstance()
    }

    private fun setupProfileInfo() {
        val userId = auth.currentUser?.uid ?: return

        databaseRef = FirebaseDatabase.getInstance().getReference("Customers").child(userId)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("nameCustomer").getValue(String::class.java)
                val email = snapshot.child("emailCustomer").getValue(String::class.java)

                profileName.text = name ?: "No name"
                profileUsername.text = email ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupClickEvents() {
        backButton.setOnClickListener {
            finish()
        }

        logoutButton.setOnClickListener {
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

        orderNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        languageChange.setOnClickListener {
            Toast.makeText(this, "Language Settings clicked", Toast.LENGTH_SHORT).show()
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
