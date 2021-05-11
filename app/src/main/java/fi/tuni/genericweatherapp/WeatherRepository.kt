package fi.tuni.genericweatherapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

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
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // API Request objects
    private val weather = OpenWeatherMap(owmKey)
    private val pexels = Pexels(pexelsKey)

    // Separate data class for combining all requested resources
    data class WeatherPacket(
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

    // Get location name for the current coordinates
    fun fetchLocationNameAsync(lambda: (String) -> Unit) {
        thread {
            val result = weather.fetchLocationName(latitude, longitude)
            lambda(result)
        }
    }

    // Get weather for the current location, and a weather related background image
    // 3 Different requests in one thread, the delay will be long
    fun fetchWeatherAsync(lambda: (WeatherPacket) -> Unit) {
        thread {
            val weatherResult = weather.fetchWeather(latitude, longitude)
            // Choose a random background image from the weather condition's photo collection
            val photoCollection =
                getWeatherType(weatherResult.current.conditionCode).photoCollection
            val photoResult = pexels.fetchCollectionMedia(photoCollection)
            val photo = photoResult.random()
            val bitmap = bitMapFromUrl(photo.portrait)
            // Send to the lambda as a combined object
            lambda(WeatherPacket(weatherResult, photo, bitmap))
        }
    }
}
