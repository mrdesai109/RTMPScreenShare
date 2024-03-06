package com.android.zebraassistrtmp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Utils {

    companion object {

        // Function to check if permissions exist
        fun checkPermissions(context: Context): Boolean {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECORD_AUDIO
                )
            } else {
                arrayOf(Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.RECORD_AUDIO)
            }
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }

        // Function to request permissions if missing
        fun requestPermissions(activity: AppCompatActivity) {
            val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.RECORD_AUDIO
                )
            } else {
                arrayOf(Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.RECORD_AUDIO)
            }
            ActivityCompat.requestPermissions(activity, permissions, 101)
        }
    }
}