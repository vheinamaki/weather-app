package fi.tuni.genericweatherapp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.TypeConverters
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

// Database table for user-saved locations
@Entity(tableName = "locations")
data class DBLocation(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
) {
    constructor(owmLocation: OpenWeatherMap.Location) : this(
        0,
        owmLocation.name,
        owmLocation.lat,
        owmLocation.lon,
        owmLocation.country
    )
}

// Data Access Object for querying the locations table
@Dao
interface DBLocationDao {
    @Query("SELECT * FROM locations")
    fun getAll(): LiveData<List<DBLocation>>

    @Insert
    fun insertAll(vararg locs: DBLocation)

    @Delete
    fun delete(loc: DBLocation)
}

// Table for cached forecasts
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

class DBTypeConverters {
    private val mapper = jacksonObjectMapper()

    @TypeConverter
    fun rootObjectToString(rootObject: OpenWeatherMap.RootObject): String {
        return mapper.writeValueAsString(rootObject)
    }

    @TypeConverter
    fun stringToRootObject(string: String): OpenWeatherMap.RootObject {
        return mapper.readValue(string, OpenWeatherMap.RootObject::class.java)
    }

    @TypeConverter
    fun photoToString(photo: Pexels.Photo): String {
        return mapper.writeValueAsString(photo)
    }

    @TypeConverter
    fun stringToPhoto(string: String): Pexels.Photo {
        return mapper.readValue(string, Pexels.Photo::class.java)
    }
}

// DAO For cached forecasts
@Dao
interface CachedForecastDao {
    @Query("SELECT * FROM forecasts WHERE latitude = :latitude AND longitude = :longitude AND units = :units")
    fun get(
        latitude: Double,
        longitude: Double,
        units: String,
    ): List<CachedForecast>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg locs: CachedForecast)

    @Delete
    fun delete(loc: CachedForecast)
}

// The database itself
@Database(entities = [DBLocation::class, CachedForecast::class], version = 1)
@TypeConverters(DBTypeConverters::class)
abstract class WeatherAppDatabase : RoomDatabase() {
    abstract fun locationDao(): DBLocationDao
    abstract fun forecastDao(): CachedForecastDao
}
