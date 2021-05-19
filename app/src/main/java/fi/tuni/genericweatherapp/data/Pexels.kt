package fi.tuni.genericweatherapp.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper class for making HTTP requests to the Pexels API
 */
class Pexels(var apiKey: String, var apiUrl: String = "https://api.pexels.com/v1") {
    private val mapper = jacksonObjectMapper()

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Photo(var photographer: String, @JsonProperty("url") var pageUrl: String) {
        lateinit var original: String
        lateinit var portrait: String
        lateinit var landscape: String

        @JsonProperty("src")
        fun unpackSources(src: Map<String, String>) {
            original = src["original"]!!
            portrait = src["portrait"]!!
            landscape = src["landscape"]!!
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MediaResponse(
        var media: Array<Photo>
    )

    // Fetch a photo collection with its ID
    fun fetchCollectionMedia(id: String): Array<Photo> {
        val url = URL("$apiUrl/collections/$id?type=photo")
        val urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.setRequestProperty("Authorization", apiKey)
        urlConnection.connect()
        val response = mapper.readValue(
            BufferedInputStream(urlConnection.inputStream),
            MediaResponse::class.java
        )
        urlConnection.disconnect()
        return response.media
    }
}
