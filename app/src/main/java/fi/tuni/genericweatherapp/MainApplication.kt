package fi.tuni.genericweatherapp

import android.app.Application
import android.content.res.Resources
import android.location.Geocoder
import androidx.room.Room
import dagger.hilt.android.HiltAndroidApp
import java.util.*

// Global application state, used by Hilt for di service management.
// Also used to store singleton objects that need to be initialized with a Context but are used in
// classes where context is not available.
@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Location database for saved locations
        database = Room.databaseBuilder(this, WeatherAppDatabase::class.java, "weather3-locations.db")
            .build()
        // Resources for UI strings etc
        res = resources
    }

    companion object {
        lateinit var database: WeatherAppDatabase
        lateinit var res: Resources
    }
}
