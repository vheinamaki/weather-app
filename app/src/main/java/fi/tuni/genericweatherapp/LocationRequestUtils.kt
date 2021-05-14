package fi.tuni.genericweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

fun requestLocation(
    client: FusedLocationProviderClient,
    activity: ComponentActivity,
    lambda: (LocationResult) -> Unit
) {
    val perm = Manifest.permission.ACCESS_FINE_LOCATION
    val alreadyGranted =
        ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED

    val request = LocationRequest.create()
    request.interval = 1000
    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            lambda(result)
            client.removeLocationUpdates(this)
        }
    }

    // Check location usage permissions
    if (alreadyGranted) {
        // Permission has already been granted, register callback
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    } else {
        // Ask for permission
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Permission granted, register callback
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            } else {
                // Rejected
                Log.d("weatherDebug", "Location permission rejected")
            }
        }.launch(perm)
    }
}
