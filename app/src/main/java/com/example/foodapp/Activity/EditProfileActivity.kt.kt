package com.example.foodapp.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.foodapp.Cloudinary.FileUtils
import com.example.foodapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.EmailAuthProvider
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

    private var imageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private val CLOUD_NAME = "durfebos5"
    private val UPLOAD_PRESET = "unsigned_android_upload"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initFirebase()
        setupImagePicker()
        setupClickEvents()
        loadCurrentUserData()
    }

    private fun initFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Customers")
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
                imageUri = result.data?.data
                imageUri?.let { uri ->
                    Glide.with(this).load(uri).into(binding.avatarImage)
                    uploadImageToCloudinary(uri)
                }
            }
        }
    }

    private fun setupClickEvents() {
        // 🔙 Back button click
        binding.backButton.setOnClickListener {
            finish()
        }

        // 🖼️ Avatar click chọn ảnh
        binding.avatarImage.setOnClickListener {
            openImagePicker()
        }

        // 💾 Save button click
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        imagePickerLauncher.launch(intent)
    }

    private fun loadCurrentUserData() {
        val currentUserId = auth.currentUser?.uid ?: return

        database.child(currentUserId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        binding.editName.setText(snapshot.child("nameCustomer").value?.toString() ?: "")
                        binding.editEmail.setText(snapshot.child("emailCustomer").value?.toString() ?: "")
                        binding.editPhone.setText(snapshot.child("phoneNumberCustomer").value?.toString() ?: "")
                        binding.editAddress.setText(snapshot.child("addressCustomer").value?.toString() ?: "")

                        val avatarUrl = snapshot.child("avatarUrl").value?.toString()
                        if (!avatarUrl.isNullOrEmpty()) {
                            Glide.with(this@EditProfileActivity)
                                .load(avatarUrl)
                                .placeholder(com.example.foodapp.R.drawable.profile)
                                .error(com.example.foodapp.R.drawable.profile)
                                .into(binding.avatarImage)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@EditProfileActivity, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditProfileActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun validateInputs(): Boolean {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()

        // Clear previous errors
        binding.editName.error = null
        binding.editEmail.error = null
        binding.editPhone.error = null

        // Validate name
        if (name.isEmpty()) {
            binding.editName.error = "Name is required"
            binding.editName.requestFocus()
            return false
        }

        if (name.length < 2) {
            binding.editName.error = "Name must be at least 2 characters"
            binding.editName.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            binding.editEmail.error = "Email is required"
            binding.editEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Invalid email format"
            binding.editEmail.requestFocus()
            return false
        }

        // Validate phone (optional but if provided, should be valid)
        if (phone.isNotEmpty() && phone.length < 10) {
            binding.editPhone.error = "Phone number must be at least 10 digits"
            binding.editPhone.requestFocus()
            return false
        }

        return true
    }

    private fun saveProfile() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()

        val currentUserId = auth.currentUser?.uid ?: return

        // Disable save button to prevent multiple clicks
        binding.saveButton.isEnabled = false
        binding.saveButton.text = "Saving..."

        val updatedUser = mapOf(
            "nameCustomer" to name,
            "emailCustomer" to email,
            "phoneNumberCustomer" to phone,
            "addressCustomer" to address
        )

        database.child(currentUserId).updateChildren(updatedUser)
            .addOnSuccessListener {
                // Update Firebase Auth email if changed
                val currentUser = auth.currentUser
                if (email != currentUser?.email) {
                    updateAuthEmail(email)
                } else {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Database error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.saveButton.isEnabled = true
                binding.saveButton.text = "Save"
            }
    }

    private fun updateAuthEmail(newEmail: String) {
        val user = auth.currentUser ?: return

        user.updateEmail(newEmail)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile and email updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { exception ->
                // If email update fails, it might need re-authentication
                Toast.makeText(this, "Profile updated, but email update failed: ${exception.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        try {
            val filePath = FileUtils.getPath(this, uri) ?: return
            val file = File(filePath)

            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show()
                return
            }

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
                        Toast.makeText(this@EditProfileActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string() ?: ""
                        val imageUrl = JSONObject(responseBody).optString("secure_url")
                        val userId = auth.currentUser?.uid ?: return

                        if (imageUrl.isNotEmpty()) {
                            database.child(userId).child("avatarUrl").setValue(imageUrl)
                                .addOnSuccessListener {
                                    runOnUiThread {
                                        Toast.makeText(this@EditProfileActivity, "Avatar updated successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    runOnUiThread {
                                        Toast.makeText(this@EditProfileActivity, "Failed to save avatar URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@EditProfileActivity, "Invalid response from server", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@EditProfileActivity, "Error processing response: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}