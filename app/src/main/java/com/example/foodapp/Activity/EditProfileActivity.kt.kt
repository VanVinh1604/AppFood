package com.example.foodapp.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodapp.Cloudinary.FileUtils
import com.example.foodapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    private val CLOUD_NAME = "durfebos5"
    private val UPLOAD_PRESET = "unsigned_android_upload"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Customers")

        val currentUserId = auth.currentUser?.uid

        // 🟢 Load thông tin người dùng
        currentUserId?.let { uid ->
            database.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.editName.setText(snapshot.child("nameCustomer").value.toString())
                        binding.editEmail.setText(snapshot.child("emailCustomer").value.toString())
                        binding.editPhone.setText(snapshot.child("phoneNumberCustomer").value.toString())
                        binding.editAddress.setText(snapshot.child("addressCustomer").value.toString())
//                        binding.editPassword.setText(snapshot.child("passwordCustomer").value.toString())
                        val avatarUrl = snapshot.child("avatarUrl").value?.toString()
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@EditProfileActivity).load(avatarUrl).into(binding.avatarImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.saveButton.setOnClickListener {
            val name = binding.editName.text.toString()
            val email = binding.editEmail.text.toString()
            val phone = binding.editPhone.text.toString()
            val address = binding.editAddress.text.toString()
//            val password = binding.editPassword.text.toString()

            currentUserId?.let { uid ->

                val updatedUser = mapOf(
                    "nameCustomer" to name,
                    "emailCustomer" to email,
                    "phoneNumberCustomer" to phone,
                    "addressCustomer" to address,
//                    "passwordCustomer" to password
                )

                database.child(uid).updateChildren(updatedUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
                    }

                val user = auth.currentUser

                if (email != user?.email) {
                    user?.updateEmail(email)
                }
//                user?.updatePassword(password)
            }
        }

        // 🖼 Avatar click chọn ảnh
        binding.avatarImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // 🔄 Sau khi chọn ảnh
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data?.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(binding.avatarImage)

            imageUri?.let { uri ->
                uploadImageToCloudinary(uri)
            }
        }
    }

    // ⬆️ Upload ảnh lên Cloudinary
    private fun uploadImageToCloudinary(uri: Uri) {
        val filePath = FileUtils.getPath(this, uri) ?: return
        val file = File(filePath)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Upload thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val imageUrl = JSONObject(response.body?.string() ?: "").optString("secure_url")
                val userId = auth.currentUser?.uid ?: return

                if (imageUrl.isNotEmpty()) {
                    database.child(userId).child("avatarUrl").setValue(imageUrl)
                        .addOnSuccessListener {
                            runOnUiThread {
                                Toast.makeText(this@EditProfileActivity, "Ảnh đại diện đã cập nhật!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        })
    }

}
