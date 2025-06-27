package com.example.foodapp.Activity

<<<<<<< HEAD
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodapp.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
=======
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Gán view
        val profileName = findViewById<TextView>(R.id.profile_name)
        val profileUsername = findViewById<TextView>(R.id.profile_username)
        val profileImage = findViewById<ImageView>(R.id.profile_image)
        val backButton = findViewById<ImageView>(R.id.back_button)
        val logoutButton = findViewById<Button>(R.id.logout_button)

        val changeInfo = findViewById<LinearLayout>(R.id.change_info)
        val orderNotification = findViewById<LinearLayout>(R.id.order_notification)
        val languageChange = findViewById<LinearLayout>(R.id.language_change)
        val aboutApp = findViewById<LinearLayout>(R.id.about_app)

        // Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
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

        // Nút quay lại
        backButton.setOnClickListener {
            finish()
        }

        // Đăng xuất
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UserLoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Chuyển sang Edit Profile
        changeInfo.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // 🔔 Chuyển sang NotificationActivity
        orderNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        // 🌐 Chưa làm
        languageChange.setOnClickListener {
            Toast.makeText(this, "Language Settings clicked", Toast.LENGTH_SHORT).show()
        }

        // ℹ️ Chuyển sang AboutActivity
        aboutApp.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }
}
>>>>>>> main
