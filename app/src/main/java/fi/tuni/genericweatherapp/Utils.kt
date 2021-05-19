package fi.tuni.genericweatherapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*

/**
 * Utility functions and configurable constant values.
 */

/**
 * OpenWeatherMap API key.
 */
const val OPENWEATHERMAP_APIKEY = keys.owmKey

/**
 * Pexels API key.
 */
const val PEXELS_APIKEY = keys.pexelsKey

/**
 * Name of the database file.
 */
const val DB_NAME = "weather3-locations.db"

/**
 * Location request timeout; How long location requests should wait for a result before firing the
 * error callback, in milliseconds.
 */
const val GEOLOC_TIMEOUT_MILLIS = 15000L // 15 seconds

/**
 * How long a forecast request should be cached for, in milliseconds.
 *
 * When a forecast request is made, its result value is stored in the database and any subsequent
 * requests with identical parameters, made within this timeframe, are discarded and the stored
 * request is returned instead.
 */
const val FORECAST_CACHE_AGE_MILLIS = 3600000L // 1h

/**
 * Weather conditions with their associated photo collections' IDs on Pexels.
 *
 * @property id The string ID of the Pexels photo collection. Available collections are specific
 * to the API key.
 */
enum class PhotoCollection(val id: String) {
    RAIN("hlfvh66"),
    CLEAR("9ouwlqp"),
    CLOUDS("9o3bjjb"),
    STORM("bpoijxy"),
    SNOW("2k6ae11"),
    MIST("o7j0pjq")
}

/**
 * Maps OpenWeatherMap condition codes to photo collection IDs.
 *
 * @param conditionCode Weather condition code as returned by OpenWeatherMap API calls.
 * @return The corresponding collection ID.
 */
fun weatherCodeToPhotoCollectionId(conditionCode: Int): String {
    return when (conditionCode) {
        in 200..299 -> PhotoCollection.STORM.id
        in 300..599 -> PhotoCollection.RAIN.id
        in 600..699 -> PhotoCollection.SNOW.id
        in 700..799 -> PhotoCollection.MIST.id
        in 801..899 -> PhotoCollection.CLOUDS.id
        else -> PhotoCollection.CLEAR.id
    }
}

/**
 * Maps OpenWeatherMap icon names to local icon resources.
 *
 * @param iconName Icon name as returned by OpenWeatherMap API calls.
 * @return Drawable resource corresponding to the weather icon.
 */
fun weatherIconToDrawableResource(iconName: String): Int {
    return when (iconName) {
        "01d", "01n" -> R.drawable.day_sunny
        "02d", "02n" -> R.drawable.day_cloudy
        "03d", "03n" -> R.drawable.cloud
        "04d", "04n" -> R.drawable.cloudy
        "09d", "09n" -> R.drawable.showers
        "10d", "10n" -> R.drawable.rain
        "11d", "11n" -> R.drawable.thunderstorm
        "13d", "13n" -> R.drawable.snow
        else -> R.drawable.fog
    }
}

/**
 * Show an alert dialog with a single "OK" Button to dismiss it.
 *
 * @param context Activity context to show the dialog in.
 * @param titleRes String resource to use as the dialog's title.
 * @param descriptionRes String resource to use as the dialog's message.
 */
fun showAlert(context: Context, titleRes: Int, descriptionRes: Int) {
    AlertDialog.Builder(context)
        .setTitle(titleRes)
        .setMessage(descriptionRes)
        .setNeutralButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

/**
 * One-time location request using Google Play Fused Location API with automatic cancellation
 * timeout.
 *
 * Makes a single geolocation request using the Fused location API. Assumes that location permission
 * has been given beforehand.
 *
 * @param client Fused client to make the request with-
 * @param lambda Callback to fire when the location request finishes. Called with a [LocationResult]
 * object, which is null if the request fails.
 */
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

    timer = object : CountDownTimer(GEOLOC_TIMEOUT_MILLIS, GEOLOC_TIMEOUT_MILLIS) {
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
