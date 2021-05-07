package fi.tuni.genericweatherapp

import android.util.Log
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URL
import java.net.URLEncoder
import java.util.*

class OpenWeatherMap(
    var apiKey: String,
    var units: String = "metric",
    var language: String = "en",
    var apiUrl: String = "https://api.openweathermap.org"
) {
    private val mapper = jacksonObjectMapper()

    abstract class Weather {
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
            // OpenWeatherMap dt property is in seconds, Java Date in millis
            time = Date(dt * 1000)
        }
    }

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
    ) : Weather()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Daily(
        var temp: DayTemps,
        @JsonProperty("feels_like")
        var feelsLike: DayTemps,
        var pressure: Int,
        var humidity: Int,
        @JsonProperty("wind_speed")
        var windSpeed: Double,
        @JsonProperty("wind_deg")
        var windDeg: Double
    ) : Weather()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DayTemps(
        @JsonProperty("morn")
        var morning: Double,
        var day: Double,
        @JsonProperty("eve")
        var evening: Double,
        var night: Double
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
        val country: String,
        val state: String?
    )

    fun fetchWeather(latitude: Double, longitude: Double): RootObject {
        val url =
            URL("$apiUrl/data/2.5/onecall?lat=$latitude&lon=$longitude&exclude=minutely&lang=$language&units=$units&appid=$apiKey")
        return mapper.readValue(url, RootObject::class.java)
    }

    fun fetchCoordinates(locationName: String): Array<Location> {
        val sanitized = URLEncoder.encode(locationName, "UTF-8")
        Log.d("weatherDebug", sanitized)
        val url = URL("$apiUrl/geo/1.0/direct?q=$sanitized&appid=$apiKey")
        return mapper.readValue(url, Array<Location>::class.java)
    }

    fun fetchLocationName(latitude: Double, longitude: Double): String {
        val url = URL("$apiUrl/geo/1.0/reverse?lat=$latitude&lon=$longitude&appid=$apiKey")
        return mapper.readValue(url, Array<Location>::class.java)[0].name
    }
}
