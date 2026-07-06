package com.reproflex3.rplink.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

var permissionDenialCount = 0

val REQUIRED_PERMISSIONS =
    mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).toTypedArray()

const val PERMISSION_DENIAL_KEY = "PERMISSION_DENIAL_KEY"

fun Fragment.permissionCheck(): Boolean = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(
        requireContext(), it
    ) == PackageManager.PERMISSION_GRANTED
}

inline fun Fragment.activityResultLauncher(
    crossinline permissionGrantedAction: () -> Unit,
    crossinline permissionDeniedAction: (Int) -> Unit
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
    { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                permissionGranted = false
            }
        }
        if (!permissionGranted) {
            permissionDenialCount++
            permissionDeniedAction.invoke(permissionDenialCount)
        } else {
            permissionGrantedAction.invoke()
        }
    }
}