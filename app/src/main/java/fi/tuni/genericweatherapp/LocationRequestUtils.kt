package fi.tuni.genericweatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

// Location request timeout; If it takes more than 15 seconds then fire error callback
private const val TIMEOUT = 15000L

fun requestLocation(
    client: FusedLocationProviderClient,
    activity: ComponentActivity,
    lambda: (LocationResult?) -> Unit
) {
    val perm = Manifest.permission.ACCESS_FINE_LOCATION
    val alreadyGranted =
        ContextCompat.checkSelfPermission(activity, perm) == PackageManager.PERMISSION_GRANTED
    lateinit var timer: CountDownTimer

    val request = LocationRequest.create()
    request.interval = 1000
    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            timer.cancel()
            lambda(result)
            client.removeLocationUpdates(this)
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            val available = availability.isLocationAvailable
            Log.d("weatherDebug", "Location available: $available")
            if (!available) {
                timer.cancel()
                lambda(null)
                client.removeLocationUpdates(this)
            }
        }
    }

    timer = object : CountDownTimer(TIMEOUT, TIMEOUT) {
        override fun onTick(p0: Long) {}

        override fun onFinish() {
            Log.d("weatherDebug", "Timeout reached")
            lambda(null)
            client.removeLocationUpdates(callback)
        }
    }

    // Check location usage permissions
    if (alreadyGranted) {
        // Permission has already been granted, register callback
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        Log.d("weatherDebug", "Start location request")
        timer.start()
    } else {
        // Ask for permission
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                // Permission granted, register callback
                client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                Log.d("weatherDebug", "Start location request")
                timer.start()
            } else {
                // Rejected
                Log.d("weatherDebug", "Location permission rejected")
            }
        }.launch(perm)
    }
}
