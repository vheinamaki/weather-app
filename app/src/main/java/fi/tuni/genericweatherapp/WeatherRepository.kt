package fi.tuni.genericweatherapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

// 1h cache for location requests
private const val CACHE_AGE = 3600000L

// Weather conditions with their associated photo collections on Pexels
enum class WeatherType(val photoCollection: String) {
    RAIN("hlfvh66"),
    CLEAR("9ouwlqp"),
    CLOUDS("9o3bjjb"),
    STORM("bpoijxy"),
    SNOW("2k6ae11"),
    MIST("o7j0pjq")
}

// Weather and background image handling, caching

// Pexels api limits: 200/hour and 20,000/month
// OWM api limits: 60/minute and 1,000,000/month

/**
 * Makes http requests to the weather/photo APIs
 *
 * Initialized by Hilt as a singleton so that the same instance is preserved throughout
 * the application life cycle.
 */
@Singleton
class WeatherRepository @Inject constructor() {
    // Latitude and longitude of the current location
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    // API Request objects
    private val weather = OpenWeatherMap(owmKey)
    private val pexels = Pexels(pexelsKey)

    // Access cached forecasts
    private val forecastDao = MainApplication.database.forecastDao()

    private val observableUnits = MutableLiveData<String>()

    var units: String
        get() = this.weather.units
        set(value) {
            this.weather.units = value
        }

    // Separate data class for combining all requested resources
    data class WeatherPacket(
        val locationName: String,
        val weather: OpenWeatherMap.RootObject,
        val photo: Pexels.Photo,
        val bitmap: Bitmap
    )

    // Maps OpenWeatherMap condition codes to different weather types
    private fun getWeatherType(conditionCode: Int): WeatherType {
        return when (conditionCode) {
            in 200..299 -> WeatherType.STORM
            in 300..599 -> WeatherType.RAIN
            in 600..699 -> WeatherType.SNOW
            in 700..799 -> WeatherType.MIST
            in 801..899 -> WeatherType.CLOUDS
            else -> WeatherType.CLEAR
        }
    }

    fun informUnitsChanged() {
        observableUnits.postValue(units)
    }

    fun unitsChanged(): LiveData<String> = observableUnits

    // Helper method get a Bitmap image for photo url
    private fun bitMapFromUrl(url: String): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val bmp = BitmapFactory.decodeStream(connection.inputStream)
        connection.disconnect()
        return bmp
    }

    fun changeLocation(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
    }

    // Get weather for the current location, and a weather related background image
    fun fetchWeatherAsync(lambda: (WeatherPacket?) -> Unit) {
        // TODO: Check if the last request was same as current, and if enough time has passed
        thread {
            val cacheResult = forecastDao.get(latitude, longitude, units).getOrNull(0)
            if (cacheResult != null && (System.currentTimeMillis() - cacheResult.timeStamp) < CACHE_AGE) {
                Log.d("weatherDebug", "Using cached result")
                val remaining =
                    (CACHE_AGE - (System.currentTimeMillis() - cacheResult.timeStamp)) / 60000.0
                Log.d(
                    "weatherDebug",
                    "Cache time remaining: $remaining min"
                )
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
                lambda(packet)
            } else {
                // Send weather forecast to the lambda as a combined object, or null if a request fails
                val packet = try {
                    val weatherResult = weather.fetchWeather(latitude, longitude)
                    // Choose a random background image from the weather condition's photo collection
                    val photoCollection =
                        getWeatherType(weatherResult.current.conditionCode).photoCollection
                    val photoResult = pexels.fetchCollectionMedia(photoCollection)
                    val photo = photoResult.random()
                    val bitmap = bitMapFromUrl(photo.portrait)
                    // Geocode the location's name
                    val name = weather.fetchLocationName(latitude, longitude)
                    val timeStamp = System.currentTimeMillis()
                    forecastDao.insertAll(
                        CachedForecast(
                            name,
                            latitude,
                            longitude,
                            units,
                            timeStamp,
                            weatherResult,
                            photo
                        )
                    )
                    // 'packet' evaluates to this if everything succeeds
                    WeatherPacket(name, weatherResult, photo, bitmap)
                } catch (e: Exception) {
                    null
                }
                lambda(packet)
            }
        }
    }
}
