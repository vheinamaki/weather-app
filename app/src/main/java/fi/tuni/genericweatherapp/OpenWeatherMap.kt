package fi.tuni.genericweatherapp

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.TypeParceler
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


    // Data model classes are parcelable and can be parsed by jackson
    // Parcelable constructor
    @Parcelize
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Hourly(
        var conditionCode: Int,
        var title: String,
        var description: String,
        var icon: String,
        var time: Date,
        var temp: Double,
        var feelsLike: Double,
        var pressure: Int,
        var humidity: Int,
        var windSpeed: Double,
        var windDeg: Double
    ) : Parcelable {

        // Jackson constructor
        @JsonCreator
        constructor(
            @JsonProperty("weather") weathers: Array<Map<String, String>>,
            @JsonProperty dt: Long,
            @JsonProperty temp: Double,
            @JsonProperty("feels_like") feelsLike: Double,
            @JsonProperty pressure: Int,
            @JsonProperty humidity: Int,
            @JsonProperty("wind_speed") windSpeed: Double,
            @JsonProperty("wind_deg") windDeg: Double
        ) : this(
            weathers[0]["id"]!!.toInt(),
            weathers[0]["main"]!!,
            weathers[0]["description"]!!,
            weathers[0]["icon"]!!,
            Date(dt * 1000),
            temp,
            feelsLike,
            pressure,
            humidity,
            windSpeed,
            windDeg
        )
    }

    // Parcelable constructor
    @Parcelize
    @TypeParceler<DayTemps, DayTempsParceler>()
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Daily(
        var conditionCode: Int,
        var title: String,
        var description: String,
        var icon: String,
        var time: Date,
        var temp: DayTemps,
        var feelsLike: DayTemps,
        var pressure: Int,
        var humidity: Int,
        var windSpeed: Double,
        var windDeg: Double
    ) : Parcelable {

        // Jackson constructor
        @JsonCreator
        constructor(
            @JsonProperty("weather") weathers: Array<Map<String, String>>,
            @JsonProperty dt: Long,
            @JsonProperty temp: DayTemps,
            @JsonProperty("feels_like") feelsLike: DayTemps,
            @JsonProperty pressure: Int,
            @JsonProperty humidity: Int,
            @JsonProperty("wind_speed") windSpeed: Double,
            @JsonProperty("wind_deg") windDeg: Double
        ) : this(
            weathers[0]["id"]!!.toInt(),
            weathers[0]["main"]!!,
            weathers[0]["description"]!!,
            weathers[0]["icon"]!!,
            Date(dt * 1000),
            temp,
            feelsLike,
            pressure,
            humidity,
            windSpeed,
            windDeg
        )
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class DayTemps(
        @JsonProperty("morn")
        var morning: Double,
        var day: Double,
        @JsonProperty("eve")
        var evening: Double,
        var night: Double
    )

    object DayTempsParceler : Parceler<DayTemps> {
        override fun create(parcel: Parcel) = DayTemps(
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readDouble()
        )

        override fun DayTemps.write(parcel: Parcel, flags: Int) {
            parcel.writeDouble(morning)
            parcel.writeDouble(day)
            parcel.writeDouble(evening)
            parcel.writeDouble(night)
        }
    }


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
