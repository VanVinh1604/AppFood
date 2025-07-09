package com.example.foodapp.Activity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.R
import com.example.foodapp.ViewModel.UserViewModel

class ForgetEmailActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var btnSendReset: Button
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_email)

        edtEmail = findViewById(R.id.edtEmail)
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnSendReset = findViewById(R.id.btnSendReset)
        viewModel =UserViewModel()

        btnSendReset.setOnClickListener {
            val email = edtEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.sendPasswordReset(email) { success, errorMessage ->
                if (success) {
                    Toast.makeText(this, "Password recovery email sent. Check your inbox.!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this, "Lỗi: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}

