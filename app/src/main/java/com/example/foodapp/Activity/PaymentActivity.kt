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
import com.example.project1762.Helper.PaypalHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private val paymentViewModel: MainViewModel by viewModels()
    private var isPaypalApproved = false
    private var appliedVoucherCode: String? = null
    private var discountAmount: Double = 0.0
    private var finalTotalPrice: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hiển thị tổng tiền được truyền từ CartActivity
        val originalPrice = intent.getStringExtra("totalPrice") ?: "0.0"
        finalTotalPrice = originalPrice
        binding.totalInput.setText("$${originalPrice}")
        // Lắng nghe dữ liệu người dùng từ ViewModel
        observeUserInfo()

        // Nút quay lại
        binding.backButton.setOnClickListener {
            finish()
        }
        initVoucherApply()

        binding.ocdBtn.setOnClickListener {
            val method = when (binding.paymentContainer.checkedRadioButtonId) {
                R.id.radioCOD -> "COD"
                R.id.radioMomo -> "Momo"
                R.id.radioPaypal -> "PayPal"
                else -> null
            }

            if (method == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (method == "PayPal") {
                val amount = finalTotalPrice ?: intent.getStringExtra("totalPrice") ?: "0.00"
                startPaypalPayment(amount)
            } else {
                placeOrder(method)
            }
        }

    }
    override fun onResume() {
        super.onResume()
        handlePaypalResult(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePaypalResult(intent)
    }

    private fun observeUserInfo() {
        paymentViewModel.getUserInfo().observe(this) { user ->
            if (user != null) {
                // Nếu người dùng đã có thông tin thì tự động điền
                user.nameCustomer?.let { binding.nameInput.setText(it) }
                user.addressCustomer?.let { binding.addressInput.setText(it) }
                user.phoneNumberCustomer?.let { binding.phoneInput.setText(it) }
            }
        }
    }

    private fun placeOrder(paymentMethod: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin cá nhân", Toast.LENGTH_SHORT).show()
            return
        }

        val order = OrderDetails(
            customerId = uid,
            customerName = name,
            address = address,
            phoneNumber = phone,
            note = binding.noteInput.text.toString(),
            totalPrice = finalTotalPrice,
            drinkNames = intent.getStringArrayListExtra("drinkNames"),
            drinkImages = intent.getStringArrayListExtra("drinkImages"),
            drinkPrices = intent.getStringArrayListExtra("drinkPrices"),
            drinkQuantities = intent.getIntegerArrayListExtra("drinkQuantities"),
            drinkSizes = intent.getStringArrayListExtra("drinkSizes"),
            paymentStatus = paymentMethod,
            currentTime = System.currentTimeMillis(),

            voucherCode = appliedVoucherCode,
            discountAmount = if (discountAmount > 0.0) "%.2f".format(discountAmount) else null
        )

        paymentViewModel.saveOrder(order) { success ->
            if (success)
            {
                appliedVoucherCode?.let { code ->
                    paymentViewModel.decreaseVoucherUsage(code)
                    paymentViewModel.markVoucherUsed(code, uid)
                }
                ManagmentCart(this).clearCart()
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

    //Payment Paypal
    private fun handlePaypalResult(intent: Intent?) {
        val uri = intent?.data ?: return

        if (uri.scheme == "myapp" && uri.host == "paypal") {
            when (uri.path) {
                "/success" -> {
                    if (!isPaypalApproved) {
                        isPaypalApproved = true
                        Toast.makeText(this, "Thanh toán PayPal thành công", Toast.LENGTH_SHORT).show()
                        placeOrder("PayPal")
                    }
                }

                "/cancel" -> {
                    Toast.makeText(this, "Bạn đã huỷ thanh toán PayPal", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun startPaypalPayment(totalAmount: String) {
        PaypalHelper.createOrder(this, totalAmount) { approvalUrl ->
            if (approvalUrl != null) {
                runOnUiThread {
                    PaypalHelper.openPaypalCheckout(this, approvalUrl)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Tạo đơn PayPal thất bại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initVoucherApply() {
        binding.buttonApply.setOnClickListener {
            val code = binding.editTextDiscount.text.toString().trim()
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            if (code.isNotEmpty() && uid != null) {
                paymentViewModel.validateAndCheckVoucher(code, uid)
            } else {
                Toast.makeText(this, "Không có mã giảm giá nào được áp dụng", Toast.LENGTH_SHORT).show()
            }
        }

        paymentViewModel.voucherLiveData.observe(this) { (voucher, error) ->
            if (voucher != null) {
                val totalStr = intent.getStringExtra("totalPrice") ?: "0"
                val total = totalStr.replace("$", "").toDoubleOrNull() ?: 0.0

                if (total >= voucher.minOrderValue) {
                    val percentDiscount = total * voucher.discountPercent / 100
                    val discount = voucher.maxDiscount?.let { max ->
                        percentDiscount.coerceAtMost(max)
                    } ?: percentDiscount

                    val finalTotal = total - discount

                    appliedVoucherCode = voucher.code
                    discountAmount = discount
                    finalTotalPrice = "%.2f".format(finalTotal)

                    binding.totalInput.setText("$$finalTotalPrice")
                    binding.buttonApply.isEnabled = false // tránh áp lại mã

                    Toast.makeText(
                        this,
                        "Áp dụng mã giảm giá thành công! Giảm $${"%.2f".format(discount)}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Đơn hàng chưa đủ điều kiện để áp dụng mã này", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, error ?: "Mã giảm giá không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }
    }

}