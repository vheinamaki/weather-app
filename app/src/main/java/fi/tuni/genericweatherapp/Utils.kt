package fi.tuni.genericweatherapp

import android.Manifest
import android.annotation.SuppressLint
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


// One-time location request with automatic cancellation timeout
@SuppressLint("MissingPermission")
fun requestLocation(
    client: FusedLocationProviderClient,
    lambda: (result: LocationResult?) -> Unit
) {
    lateinit var timer: CountDownTimer

    val request = LocationRequest.create()
    request.interval = 1000
    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Log.d(
                "weatherDebug",
                "Location request successful: ${result.lastLocation.latitude}, ${result.lastLocation.longitude}"
            )
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

    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    Log.d("weatherDebug", "Start location request")
    timer.start()
}