package com.example.foodapp.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        val currentPassword = findViewById<EditText>(R.id.current_password)
        val newPassword = findViewById<EditText>(R.id.new_password)
        val confirmNewPassword = findViewById<EditText>(R.id.confirm_new_password)
        val changeButton = findViewById<Button>(R.id.btn_change_password)

        changeButton.setOnClickListener {
            val current = currentPassword.text.toString()
            val newPass = newPassword.text.toString()
            val confirm = confirmNewPassword.text.toString()

            if (newPass != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            val email = user?.email ?: return@setOnClickListener

            // Re-authenticate
            val credential = EmailAuthProvider.getCredential(email, current)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPass)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Incorrect current password", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
