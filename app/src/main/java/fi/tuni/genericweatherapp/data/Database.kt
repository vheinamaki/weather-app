package fi.tuni.genericweatherapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.TypeConverters
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Database configuration using Android Room API
 */

/**
 * Database table model for user-saved locations.
 *
 * @param uid Table's primary key
 * @param name Name of the location itself, e.g. city name.
 * @param latitude Latitude of the location.
 * @param longitude of the location.
 * @param country Country of the location, stored as ISO alpha 2 country code.
 */
@Entity(tableName = "locations")
data class DBLocation(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
) {
    /**
     * @constructor Construct a database location object from an
     * [OpenWeatherMap.Location] object.
     */
    constructor(owmLocation: OpenWeatherMap.Location) : this(
        0,
        owmLocation.name,
        owmLocation.lat,
        owmLocation.lon,
        owmLocation.country
    )
}

/**
 * Data Access Object for querying the locations table.
 */
@Dao
interface DBLocationDao {
    /**
     * Retrieve all rows stored in the locations table as [LiveData].
     */
    @Query("SELECT * FROM locations")
    fun getAll(): LiveData<List<DBLocation>>

    /**
     * Insert the [locations] into the locations table.
     */
    @Insert
    fun insertAll(vararg locations: DBLocation)

    /**
     * Delete the [location] from the database, if it exists there.
     */
    @Delete
    fun delete(location: DBLocation)
}

/**
 * Database table model for cached forecasts.
 *
 * [latitude], [longitude] and [units] are used as a composite primary key to uniquely identify
 * a forecast by its request parameters.
 *
 * @param locationName Name of the location this forecast was requested for.
 * @param latitude Latitude of the location.
 * @param longitude Longitude of the location.
 * @param units The temperature units this forecast uses.
 * @param timeStamp The time when this forecast was requested,
 * in [system time][System.currentTimeMillis].
 * @param weather The stored weather forecast itself.
 * @param photo The photo object randomly chosen for this forecast.
 */
@Entity(tableName = "forecasts", primaryKeys = ["latitude", "longitude", "units"])
data class CachedForecast(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val units: String,
    val timeStamp: Long,
    val weather: OpenWeatherMap.RootObject,
    val photo: Pexels.Photo
)

/**
 * Type converters for the database. Used to serialize different objects into formats that can be
 * stored with SQLite, and deserialize them back into their original object form.
 */
private class DBTypeConverters {
    private val mapper = jacksonObjectMapper()

    /**
     * Converts a weather forecast object into JSON string.
     */
    @TypeConverter
    fun rootObjectToString(rootObject: OpenWeatherMap.RootObject): String {
        return mapper.writeValueAsString(rootObject)
    }

    /**
     * Converts a JSON string into weather forecast object.
     */
    @TypeConverter
    fun stringToRootObject(string: String): OpenWeatherMap.RootObject {
        return mapper.readValue(string, OpenWeatherMap.RootObject::class.java)
    }

    /**
     * Converts a Photo object into JSON string.
     */
    @TypeConverter
    fun photoToString(photo: Pexels.Photo): String {
        return mapper.writeValueAsString(photo)
    }

    /**
     * Converts a JSON string into Photo object.
     */
    @TypeConverter
    fun stringToPhoto(string: String): Pexels.Photo {
        return mapper.readValue(string, Pexels.Photo::class.java)
    }
}

// DAO For cached forecasts
/**
 * Data Access Object for CachedForecast objects stored in the forecasts table.
 */
@Dao
interface CachedForecastDao {
    /**
     * Query a [CachedForecast] object with matching request parameters.
     */
    @Query("SELECT * FROM forecasts WHERE latitude = :latitude AND longitude = :longitude AND units = :units")
    fun get(
        latitude: Double,
        longitude: Double,
        units: String,
    ): List<CachedForecast>

    /**
     * Insert the [forecasts] into the table, replacing ones with identical
     * latitude-longitude-units combination.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg forecasts: CachedForecast)

    /**
     * Delete the [forecast] from the database, if it exists there.
     */
    @Delete
    fun delete(forecast: CachedForecast)
}

/**
 * The app's database, gives access to saved locations and cached forecasts.
 */
@Database(entities = [DBLocation::class, CachedForecast::class], version = 1)
@TypeConverters(DBTypeConverters::class)
abstract class WeatherAppDatabase : RoomDatabase() {
    /**
     * Accesses the database's [DBLocationDao]
     */
    abstract fun locationDao(): DBLocationDao

    /**
     * Accesses the database's [CachedForecastDao]
     */
    abstract fun forecastDao(): CachedForecastDao
}
