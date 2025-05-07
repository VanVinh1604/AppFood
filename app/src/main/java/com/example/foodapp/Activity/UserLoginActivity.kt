package com.example.foodapp.Activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodapp.R
import com.example.foodapp.ViewModel.UserViewModel
import com.example.foodapp.databinding.ActivityUserLoginBinding

class UserLoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserLoginBinding
    private lateinit var viewModel: UserViewModel  // Khai báo viewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel = UserViewModel()
        binding= ActivityUserLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)


        binding.loginBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString()
            val password = binding.passTxt.text.toString()

            // Gọi phương thức login từ ViewModel hoặc Repository
            viewModel.loginUser(email, password) { success ->
                if (success) {
                    // Đăng nhập thành công, chuyển đến MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Đóng màn hình đăng nhập
                } else {
                    Toast.makeText(this, "Đăng nhập không thành công", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Đăng ký sự kiện cho nút sign up
        binding.signBtn.setOnClickListener {
            // Chuyển đến màn hình đăng ký
            val intent = Intent(this, UserSignUpActivity::class.java)
            startActivity(intent)
        }
    }
}