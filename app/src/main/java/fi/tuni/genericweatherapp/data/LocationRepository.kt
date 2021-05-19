package fi.tuni.genericweatherapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fi.tuni.genericweatherapp.MainApplication
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

/**
 * Manages the database's locations table and keeps reference to user's current geolocation.
 *
 * Initialized by Hilt as a singleton so that the same instance is preserved throughout
 * the application life cycle.
 */
@Singleton
class LocationRepository @Inject constructor() {
    /**
     * Current geolocation. Not saved to the database, only cached for the lifetime of the app.
     */
    private var currenLocation = MutableLiveData<DBLocation>()

    /**
     * Database's Data Access Object for saved locations.
     */
    private val locationDao = MainApplication.database.locationDao()

    /**
     * Observable [LiveData] for database locations, automatically updated when table contents change.
     */
    val locations = locationDao.getAll()

    /**
     * Update the current geolocation value, causing observers to be notified of the change.
     */
    fun setCurrentLocation(name: String, latitude: Double, longitude: Double) {
        currenLocation.postValue(DBLocation(-1, name, latitude, longitude, ""))
    }

    /**
     * Get the current geolocation value as [LiveData]
     */
    fun getCurrentLocation(): LiveData<DBLocation> {
        return this.currenLocation
    }

    /**
     * Insert a location into the locations table asynchronously.
     */
    fun insertLocation(location: DBLocation) {
        thread {
            locationDao.insertAll(location)
        }
    }

    /**
     * Delete a location from the locations table asynchronously.
     */
    fun deleteLocation(location: DBLocation) {
        thread {
            locationDao.delete(location)
        }
    }
}
