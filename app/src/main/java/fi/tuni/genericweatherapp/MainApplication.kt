package fi.tuni.genericweatherapp

import android.app.Application
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
        database = Room.databaseBuilder(this, LocationDatabase::class.java, "weather2-locations.db")
            .build()
        // Rest of the UI is in english as well, use a static locale for consistency
        geoCoder = Geocoder(this, Locale.ENGLISH)
    }

    companion object {
        lateinit var database: LocationDatabase
        lateinit var geoCoder: Geocoder
    }
}
