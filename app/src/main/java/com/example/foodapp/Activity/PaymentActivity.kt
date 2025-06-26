package com.example.foodapp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.foodapp.Domain.OrderDetails
import com.example.foodapp.R
import com.example.foodapp.ViewModel.MainViewModel
import com.example.foodapp.databinding.ActivityPaymentBinding
import com.example.project1762.Helper.ManagmentCart
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val paymentViewModel: MainViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hiển thị tổng tiền được truyền từ CartActivity
        val totalPrice = intent.getStringExtra("totalPrice")
        binding.totalInput.setText("$${totalPrice}")

        // Lắng nghe dữ liệu người dùng từ ViewModel
        observeUserInfo()

        // Nút quay lại
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.ocdBtn.setOnClickListener {
            placeOrder()
        }
    }
    private fun observeUserInfo() {
        paymentViewModel.getUserInfo().observe(this) { user ->
            if (user != null) {
                binding.nameInput.setText(user.nameCustomer)
                binding.addressInput.setText(user.addressCustomer)
                binding.phoneInput.setText(user.phoneNumberCustomer)
            } else {
                Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun placeOrder() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }
        val paymentMethod = when (binding.paymentContainer.checkedRadioButtonId) {
            R.id.radioCOD -> "COD"
            R.id.radioMomo -> "Momo"
            R.id.radioPaypal -> "PayPal"
            else -> null
        }
        if (paymentMethod == null) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
            return
        }

        val order = OrderDetails(
            customerId = uid,
            customerName = binding.nameInput.text.toString(),
            address = binding.addressInput.text.toString(),
            phoneNumber = binding.phoneInput.text.toString(),
            note = binding.noteInput.text.toString(),
            totalPrice = intent.getStringExtra("totalPrice"),
            drinkNames = intent.getStringArrayListExtra("drinkNames"),
            drinkImages = intent.getStringArrayListExtra("drinkImages"),
            drinkPrices = intent.getStringArrayListExtra("drinkPrices"),
            drinkQuantities = intent.getIntegerArrayListExtra("drinkQuantities"),
            drinkSizes = intent.getStringArrayListExtra("drinkSizes"),
            paymentStatus = paymentMethod,
            currentTime = System.currentTimeMillis()  // 🟢 Ghi thời điểm tạo đơn hàng
        )

        paymentViewModel.saveOrder(order) { success ->
            if (success) {
                // ✅ XÓA GIỎ HÀNG
                val cart = ManagmentCart(this)
                cart.clearCart()

                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, OrderDetailsActivity::class.java)
                intent.putExtra("order", order)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Đặt hàng thất bại!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}