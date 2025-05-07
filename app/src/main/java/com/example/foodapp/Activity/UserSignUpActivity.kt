package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodapp.Domain.CustomerModel
import com.example.foodapp.R
import com.example.foodapp.ViewModel.UserViewModel
import com.example.foodapp.databinding.ActivitySplashBinding
import com.example.foodapp.databinding.ActivityUserSignUpBinding

class UserSignUpActivity : AppCompatActivity() {
    private val viewModel: UserViewModel by viewModels()
    lateinit var binding: ActivityUserSignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserSignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val name = findViewById<EditText>(R.id.nameTxt)
        val email = findViewById<EditText>(R.id.emailTxt)
        val password = findViewById<EditText>(R.id.passTxt)
        val repassword = findViewById<EditText>(R.id.repassTxt)
        val phone = findViewById<EditText>(R.id.phoneTxt)
        val address = findViewById<EditText>(R.id.addressTxt)
        val signUpBtn = findViewById<Button>(R.id.signBtn)

        signUpBtn.setOnClickListener {
            val customer = CustomerModel(
                nameCustomer = name.text.toString(),
                emailCustomer = email.text.toString(),
                passwordCustomer = password.text.toString(),
                phoneNumberCustomer = phone.text.toString(),
                addressCustomer = address.text.toString(),
                profileImage = null // Hoặc giá trị mặc định
            )

            if (customer.nameCustomer.isNullOrEmpty() ||
                customer.emailCustomer.isNullOrEmpty() ||
                customer.passwordCustomer.isNullOrEmpty() ||
                customer.phoneNumberCustomer.isNullOrEmpty() ||
                customer.addressCustomer.isNullOrEmpty() ||
                repassword.text.toString().isNullOrEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            } else if (password.text.toString() != repassword.text.toString()) {
                Toast.makeText(this, "Mật khẩu và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.signUpCustomer(customer) { success, message ->
                    if (success) {
                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                        val intent= Intent(this, UserLoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}