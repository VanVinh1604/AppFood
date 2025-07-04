package com.example.foodapp.Cloudinary

import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

object CloudinaryManager {
    private const val CLOUD_NAME = "durfebos5" // ✅ Cloud name của bạn
    private const val UPLOAD_PRESET = "unsigned_android_upload" // ✅ Preset đã tạo ở Cloudinary

    fun uploadImage(imageUri: Uri, context: android.content.Context, callback: (String?) -> Unit) {
        val file = File(FileUtils.getPath(context, imageUri)) ?: run {
            callback(null)
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val url = Regex("\"url\":\"(.*?)\"").find(body ?: "")?.groups?.get(1)?.value
                callback(url?.replace("\\/", "/"))
            }
        })
    }
}
