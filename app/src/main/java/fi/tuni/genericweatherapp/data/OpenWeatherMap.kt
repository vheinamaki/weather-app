package fi.tuni.genericweatherapp.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Helper class for making HTTP requests to the OpenWeatherMap API
 *
 * The functions here are synchronous and will throw exceptions if the requests fail
 */
class OpenWeatherMap(
    var apiKey: String,
    var units: String = "metric",
    var language: String = "en",
    var apiUrl: String = "https://api.openweathermap.org"
) {
    private val mapper = jacksonObjectMapper()


    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Hourly(
        var temp: Double,
        @JsonProperty("feels_like")
        var feelsLike: Double,
        var pressure: Int,
        var humidity: Int,
        @JsonProperty("wind_speed")
        var windSpeed: Double,
        @JsonProperty("wind_deg")
        var windDeg: Double
    ) {
        var conditionCode: Int = 0
        lateinit var title: String
        lateinit var description: String
        lateinit var icon: String
        lateinit var time: Date

        @JsonProperty("weather")
        fun unpackWeather(weathers: Array<Map<String, String>>) {
            conditionCode = weathers[0]["id"]!!.toInt()
            title = weathers[0]["main"]!!
            description = weathers[0]["description"]!!
            icon = weathers[0]["icon"]!!
        }

        @JsonProperty("dt")
        fun convertTime(dt: Long) {
            time = Date(dt * 1000)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Daily(
        var temp: DayTemps,
        var pressure: Int,
        var humidity: Int,
        @JsonProperty("wind_speed")
        var windSpeed: Double,
        @JsonProperty("wind_deg")
        var windDeg: Double
    ) {
        var conditionCode: Int = 0
        lateinit var title: String
        lateinit var description: String
        lateinit var icon: String
        lateinit var time: Date

        @JsonProperty("weather")
        fun unpackWeather(weather: Array<Map<String, String>>) {
            conditionCode = weather[0]["id"]!!.toInt()
            title = weather[0]["main"]!!
            description = weather[0]["description"]!!
            icon = weather[0]["icon"]!!
        }

        @JsonProperty("dt")
        fun convertTime(dt: Long) {
            time = Date(dt * 1000)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DayTemps(
        @JsonProperty("morn")
        var morning: Double,
        var day: Double,
        @JsonProperty("eve")
        var evening: Double,
        var night: Double,
        var min: Double,
        var max: Double
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RootObject(
        val lat: Double,
        val lon: Double,
        val timezone: String,
        val current: Hourly,
        val hourly: Array<Hourly>,
        val daily: Array<Daily>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Location(
        val name: String,
        val lat: Double,
        val lon: Double,
        val country: String
    )

    // Get weather information from the OneCall API
    fun fetchWeather(latitude: Double, longitude: Double): RootObject {
        val url =
            URL("$apiUrl/data/2.5/onecall?lat=$latitude&lon=$longitude&exclude=minutely&lang=$language&units=$units&appid=$apiKey")
        return mapper.readValue(url, RootObject::class.java)
    }

    // Get location information (coordinates, country code) from the geocoding API
    // Used for "Add Location" search functionality
    fun fetchCoordinates(locationName: String): Array<Location> {
        val sanitized = URLEncoder.encode(locationName, "UTF-8")
        val url = URL("$apiUrl/geo/1.0/direct?q=$sanitized&appid=$apiKey")
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Authorization", apiKey)
        urlConnection.connect()
        // API may send a 404 if the location name is invalid
        val response = if (urlConnection.responseCode == 404) {
            emptyArray()
        } else {
            mapper.readValue(
                BufferedInputStream(urlConnection.inputStream),
                Array<Location>::class.java
            )
        }
        urlConnection.disconnect()
        return response
    }

    // Get location name with coordinates from the reverse geocoding API
    // Used to get name for location when using GPS coordinates, as the OneCall API does not give
    // the location name in its results.
    fun fetchLocationName(latitude: Double, longitude: Double): String {
        val url = URL("$apiUrl/geo/1.0/reverse?lat=$latitude&lon=$longitude&appid=$apiKey")
        return mapper.readValue(url, Array<Location>::class.java)[0].name
    }
}
