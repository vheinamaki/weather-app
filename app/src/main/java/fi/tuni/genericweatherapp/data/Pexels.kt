package fi.tuni.genericweatherapp.data

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper class for making HTTP requests to the Pexels API.
 */
class Pexels(var apiKey: String, var apiUrl: String = "https://api.pexels.com/v1") {
    private val mapper = jacksonObjectMapper()

    /**
     * Pexels API response containing URLs and metadata for a photograph.
     *
     * @property photographer The person/organization who took this image.
     * @property pageUrl A link to this photo's webpage on Pexels.
     * @property original URL to the photograph, with its original dimensions.
     * @property portrait URL to the photograph, cropped and compressed to portrait dimensions.
     * @property landscape URL to the photograph, cropped and compressed to landscape dimensions.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Photo(var photographer: String, @JsonProperty("url") var pageUrl: String) {
        lateinit var original: String
        lateinit var portrait: String
        lateinit var landscape: String

        /**
         * Used by Jackson to map properties in the 'src' sub-object into direct properties for the
         * class.
         */
        @JsonProperty("src")
        fun unpackSources(src: Map<String, String>) {
            original = src["original"]!!
            portrait = src["portrait"]!!
            landscape = src["landscape"]!!
        }
    }

    /**
     * Pexels API response, containing the media of a collection.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MediaResponse(
        var media: Array<Photo>
    )

    /**
     * Fetch a Pexels photo collection by its ID. Only works for photo collections owned by the API
     * key holding account.
     *
     * @param id The unique string ID of the collection.
     * @return A list containing the photos of the collection.
     */
    fun fetchCollectionMedia(id: String): Array<Photo> {
        val url = URL("$apiUrl/collections/$id?type=photo")
        val urlConnection = url.openConnection() as HttpURLConnection
        // Pexels API requires the API key as a request header rather than as a URL parameter
        urlConnection.setRequestProperty("Authorization", apiKey)
        urlConnection.connect()
        val response = mapper.readValue(
            BufferedInputStream(urlConnection.inputStream),
            MediaResponse::class.java
        )
        urlConnection.disconnect()
        // Only return the MediaResponse's array since it's the only relevant property.
        return response.media
    }
}
