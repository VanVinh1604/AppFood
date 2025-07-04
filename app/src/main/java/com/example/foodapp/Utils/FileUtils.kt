package com.example.foodapp.Cloudinary

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    fun getPath(context: Context, uri: Uri): String? {
        val fileName = getFileName(context, uri)
        if (fileName == null) return null

        val tempFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile.absolutePath
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }
}
