package fi.tuni.genericweatherapp

import android.content.Context
import androidx.room.*

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

@Dao
interface DBLocationDao {
    @Query("SELECT * FROM locations")
    fun getAll(): List<DBLocation>

    @Insert
    fun insertAll(vararg locs: DBLocation)

    @Delete
    fun delete(loc: DBLocation)
}

@Database(entities = arrayOf(DBLocation::class), version = 1)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): DBLocationDao

    companion object {
        var instance: LocationDatabase? = null

        fun getInstance(context: Context): LocationDatabase {
            return instance ?: Room.databaseBuilder(
                context,
                LocationDatabase::class.java, "weather2-locations.db"
            ).build().also {
                instance = it
            }
        }
    }
}
