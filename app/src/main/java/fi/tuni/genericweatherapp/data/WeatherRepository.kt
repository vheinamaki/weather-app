package fi.tuni.genericweatherapp.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fi.tuni.genericweatherapp.*
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

/**
 * Makes http requests to the weather/photo APIs and manages caching of the results.
 *
 * Initialized by Hilt as a singleton so that the same instance is preserved throughout
 * the application life cycle.
 */
@Singleton
class WeatherRepository @Inject constructor() {
    /**
     * Latitude used in the previous forecast request.
     */
    var previousLatitude: Double = 0.0

    /**
     * Longitude used in the previous forecast request.
     */
    var previousLongitude: Double = 0.0

    /**
     * Makes OpenWeatherMap API calls.
     */
    private val weather = OpenWeatherMap(OPENWEATHERMAP_APIKEY)

    /**
     * Makes API calls to Pexels.
     */
    private val pexels = Pexels(PEXELS_APIKEY)

    /**
     * Data Access Object for the forecast cache.
     */
    private val forecastDao = MainApplication.database.forecastDao()

    /**
     * LiveData for the units used in OpenWeatherMap API calls.
     */
    private val observableUnits = MutableLiveData<String>()

    /**
     * OpenWeatherMap API object's units, exposed for modification.
     */
    var units: String
        get() = this.weather.units
        set(value) {
            this.weather.units = value
        }

    /**
     * The resources fetched by [fetchWeatherAsync], combined into a single data class.
     */
    data class WeatherPacket(
        val locationName: String,
        val weather: OpenWeatherMap.RootObject,
        val photo: Pexels.Photo,
        val bitmap: Bitmap
    )

    /**
     * Used by [SettingsFragment] to announce a change in units configuration,
     * triggering the observer callback for views observing [unitsChanged].
     */
    fun informUnitsChanged() {
        observableUnits.postValue(units)
    }

    /**
     * Observable LiveData publishing changes to the configured units, allowing listening views to
     * update their data with the new units.
     */
    fun unitsChanged(): LiveData<String> = observableUnits

    /**
     * Helper method to get a [Bitmap] for image url
     *
     * @param url The image's URL.
     * @return Bitmap constructed from the image.
     */
    private fun bitMapFromUrl(url: String): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val bmp = BitmapFactory.decodeStream(connection.inputStream)
        connection.disconnect()
        return bmp
    }

    /**
     * Checks whether a CachedForecast has expired or not,
     * i.e. it's older than [FORECAST_CACHE_AGE_MILLIS]
     */
    private fun hasExpired(cached: CachedForecast) =
        (System.currentTimeMillis() - cached.timeStamp) < FORECAST_CACHE_AGE_MILLIS

    /**
     * Get weather for the current location, and a weather related background image.
     *
     * @param latitude Latitude of the location.
     * @param longitude longitude of the location.
     * @param lambda Callback fired when the weather request has finished. Called with
     * a [WeatherPacket] which is null if the request fails.
     */
    fun fetchWeatherAsync(latitude: Double, longitude: Double, lambda: (WeatherPacket?) -> Unit) {
        // Set new values for previous coordinates regardless of whether the request succeeds or not
        previousLatitude = latitude
        previousLongitude = longitude
        thread {
            // Check whether the cache contains a forecast with identical request parameters
            val cacheResult = forecastDao.get(latitude, longitude, units).getOrNull(0)
            if (cacheResult != null && !hasExpired(cacheResult)) {
                Log.d("weatherDebug", "Using cached result")
                val remaining =
                    (FORECAST_CACHE_AGE_MILLIS - (System.currentTimeMillis() - cacheResult.timeStamp)) / 60000.0
                Log.d(
                    "weatherDebug",
                    "Cache time remaining: $remaining min"
                )
                // If a cached forecast exists and has not expired, use it instead of
                // requesting a new one. Attempt to fetch the bitmap.
                val packet = try {
                    val bitmap = bitMapFromUrl(cacheResult.photo.portrait)
                    WeatherPacket(
                        cacheResult.locationName,
                        cacheResult.weather,
                        cacheResult.photo,
                        bitmap
                    )
                } catch (e: Exception) {
                    null
                }
                // Call the lambda argument with the cached packet
                lambda(packet)
            } else {
                // If no cached forecast exists or it has expired, request a new one
                val packet = try {
                    // Get the forecast
                    val weatherResult = weather.fetchWeather(latitude, longitude)
                    // Choose a random background image from the weather condition's photo collection
                    val photoCollection =
                        weatherCodeToPhotoCollectionId(weatherResult.current.conditionCode)
                    val photo = pexels.fetchCollectionMedia(photoCollection).random()
                    val bitmap = bitMapFromUrl(photo.portrait)
                    // Geocode the location's name
                    val name = weather.fetchLocationName(latitude, longitude)
                    // Insert a copy of the request and its result into the cache
                    forecastDao.insertAll(
                        CachedForecast(
                            name,
                            latitude,
                            longitude,
                            units,
                            System.currentTimeMillis(),
                            weatherResult,
                            photo
                        )
                    )
                    // 'packet' evaluates to this if everything succeeds
                    WeatherPacket(name, weatherResult, photo, bitmap)
                } catch (e: Exception) {
                    // 'packet' evaluates to null if exception is encountered
                    null
                }
                // Call the lambda argument with the fetched packet
                lambda(packet)
            }
        }
    }
}
