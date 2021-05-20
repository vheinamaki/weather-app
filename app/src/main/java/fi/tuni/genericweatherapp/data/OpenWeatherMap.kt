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
 * Helper class for making HTTP requests to the OpenWeatherMap API.
 *
 * The functions here are synchronous and will throw exceptions if the requests fail.
 *
 * @property apiKey The OpenWeatherMap API key to use for the requests.
 * @property units The measurement system that the API responses should use.
 * @property language The ISO alpha 2 country code for the language that the responses should use.
 * @property apiUrl Base URL of the API provider.
 */
class OpenWeatherMap(
    var apiKey: String,
    var units: String = "metric",
    var language: String = "en",
    var apiUrl: String = "https://api.openweathermap.org"
) {
    private val mapper = jacksonObjectMapper()

    /**
     * Contains properties used by both [Hourly] and [Daily] API response objects.
     */
    abstract class SharedWeather {
        var conditionCode: Int = 0
        lateinit var title: String
        lateinit var description: String
        lateinit var icon: String
        lateinit var time: Date

        /**
         * Used by Jackson to convert the 'weather' array of the API response into properties.
         *
         * Most of the time the API response only contains one item in the array, which is the
         * 'primary' weather condition. The secondary ones are not used by the app, so the primary
         * condition can just be included as direct properties.
         *
         * @param weathers The weather array contained in the API response.
         */
        @JsonProperty("weather")
        fun unpackWeather(weathers: Array<Map<String, String>>) {
            conditionCode = weathers[0]["id"]!!.toInt()
            title = weathers[0]["main"]!!
            description = weathers[0]["description"]!!
            icon = weathers[0]["icon"]!!
        }

        /**
         * Used by jackson to convert the dt Unix time field into Java Date.
         */
        @JsonProperty("dt")
        fun convertTime(dt: Long) {
            // Convert seconds to ms since 'dt' Uses Unix time and Java Date uses milliseconds.
            time = Date(dt * 1000)
        }
    }

    /**
     * OpenWeatherMap API response object containing an hourly forecast for the hour defined in
     * the [time] property.
     */
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
    ) : SharedWeather()

    /**
     * OpenWeatherMap API response object containing a daily forecast for the day defined in
     * the [time] property.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Daily(
        var temp: DayTemps,
        var pressure: Int,
        var humidity: Int,
        @JsonProperty("wind_speed")
        var windSpeed: Double,
        @JsonProperty("wind_deg")
        var windDeg: Double
    ) : SharedWeather()

    /**
     * Contains detailed information about the temperatures of a day.
     */
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

    /**
     * OpenWeatherMap API response, containing weather forecast for the location defined by [lat]
     * and [lon] for the next 48 hours and 7 days.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RootObject(
        val lat: Double,
        val lon: Double,
        val timezone: String,
        val current: Hourly,
        val hourly: Array<Hourly>,
        val daily: Array<Daily>,
    )

    /**
     * A Location returned by OpenWeatherMap geocoding API.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Location(
        val name: String,
        val lat: Double,
        val lon: Double,
        val country: String
    )

    // Get weather information from the OneCall API
    /**
     * Fetch [weather forecast][RootObject] for the given [latitude] and [longitude].
     *
     * This is a blocking function and should not be called from the UI thread.
     *
     * @return Current weather forecast for the given coordinates.
     */
    fun fetchWeather(latitude: Double, longitude: Double): RootObject {
        val url =
            URL("$apiUrl/data/2.5/onecall?lat=$latitude&lon=$longitude&exclude=minutely&lang=$language&units=$units&appid=$apiKey")
        return mapper.readValue(url, RootObject::class.java)
    }

    /**
     * Fetch location information (coordinates, country code) from the geocoding API.
     *
     * @param locationName The geographic location to search for.
     * @return A list of locations possibly matching the search string.
     */
    fun fetchCoordinates(locationName: String): Array<Location> {
        // Sanitize the input (replace spaces with +, etc)
        val sanitized = URLEncoder.encode(locationName, "UTF-8")
        val url = URL("$apiUrl/geo/1.0/direct?q=$sanitized&limit=15&appid=$apiKey")
        val urlConnection = url.openConnection() as HttpURLConnection
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

    /**
     * Get location name for [latitude] and [longitude] from the reverse geocoding API
     *
     * Used to get name for location requested with [fetchWeather], as the One Call API does not
     * give the location name in its results.
     *
     * @return Name of the location at the requested [latitude] and [longitude]
     */
    fun fetchLocationName(latitude: Double, longitude: Double): String {
        val url = URL("$apiUrl/geo/1.0/reverse?lat=$latitude&lon=$longitude&appid=$apiKey")
        return mapper.readValue(url, Array<Location>::class.java)[0].name
    }
}
