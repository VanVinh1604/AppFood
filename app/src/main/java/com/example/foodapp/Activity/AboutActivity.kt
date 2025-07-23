package com.example.foodapp.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodapp.R
import android.widget.ImageView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        // Apply padding to system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set up back button click listener
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            handleBackAction()
        }
    }

    // Handle back action for both UI button and system back
    private fun handleBackAction() {
        finish() // Simply close the activity
    }

    // Override system back button
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBackAction() // Call the same back action handler
        // Note: If using Jetpack's OnBackPressedDispatcher, see alternative below
    }

    // Alternative for Jetpack (uncomment if using newer AndroidX version)
    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_about)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            handleBackAction()
        }

        // Handle system back button using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this) {
            handleBackAction()
        }
    }
    */
}