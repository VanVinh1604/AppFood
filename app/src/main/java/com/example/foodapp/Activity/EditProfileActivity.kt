package com.example.foodapp.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Customers")

        val currentUserId = auth.currentUser?.uid

        // 🟢 Load dữ liệu người dùng
        if (currentUserId != null) {
            database.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.editName.setText(snapshot.child("nameCustomer").value.toString())
                        binding.editEmail.setText(snapshot.child("emailCustomer").value.toString())
                        binding.editPhone.setText(snapshot.child("phoneNumberCustomer").value.toString())
                        binding.editAddress.setText(snapshot.child("addressCustomer").value.toString())
                        binding.editPassword.setText(snapshot.child("passwordCustomer").value.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // 🟡 Xử lý nút Save
        binding.saveButton.setOnClickListener {
            val name = binding.editName.text.toString()
            val email = binding.editEmail.text.toString()
            val phone = binding.editPhone.text.toString()
            val address = binding.editAddress.text.toString()
            val password = binding.editPassword.text.toString()

            if (currentUserId != null && auth.currentUser != null) {

                // ✅ Cập nhật Realtime Database
                val updatedUser = mapOf(
                    "nameCustomer" to name,
                    "emailCustomer" to email,
                    "phoneNumberCustomer" to phone,
                    "addressCustomer" to address,
                    "passwordCustomer" to password
                )

                database.child(currentUserId).updateChildren(updatedUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }

                val user = auth.currentUser
                val oldEmail = user?.email

                // ✅ Nếu email thay đổi thì cập nhật FirebaseAuth email
                if (email != oldEmail) {
                    user?.updateEmail(email)?.addOnSuccessListener {
                        Toast.makeText(this, "Email updated!", Toast.LENGTH_SHORT).show()
                    }?.addOnFailureListener {
                        Toast.makeText(this, "Failed to update email: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                // ✅ Cập nhật mật khẩu
                user?.updatePassword(password)?.addOnSuccessListener {
                    Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show()
                }?.addOnFailureListener {
                    Toast.makeText(this, "Failed to update password: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 🖼 Chọn ảnh đại diện (chưa upload lên Firebase Storage)
        binding.avatarImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // 🔄 Hiển thị ảnh đại diện sau khi chọn
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.avatarImage)
        }
    }
}
