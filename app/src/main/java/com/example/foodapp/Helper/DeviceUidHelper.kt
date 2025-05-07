package com.example.foodapp.Helper

import android.content.Context
import java.util.UUID

object DeviceUidHelper {
    private const val PREF_NAME = "DeviceUIDPref"
    private const val KEY_UID = "device_uid"

    fun getDeviceUid(context: Context): String {
        val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var uid = sharedPreferences.getString(KEY_UID, null)

        if (uid == null) {
            uid = UUID.randomUUID().toString()
            sharedPreferences.edit().putString(KEY_UID, uid).apply()
        }

        return uid
    }
}
