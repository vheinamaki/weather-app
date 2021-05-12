package fi.tuni.genericweatherapp

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// Database table for user-saved locations
@Entity(tableName = "locations")
data class DBLocation(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "country") val country: String
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

// The database itself, with a singleton instance to access it
@Database(entities = [DBLocation::class], version = 1)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): DBLocationDao
}
