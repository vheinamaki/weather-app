package fi.tuni.genericweatherapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import fi.tuni.genericweatherapp.MainApplication
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

/**
 * Communicates with the location database and keeps reference to received GPS location
 *
 * Initialized by Hilt as a singleton so that the same instance is preserved throughout
 * the application life cycle.
 */
@Singleton
class LocationRepository @Inject constructor() {
    // Current GPS location. Not saved to the database, only cached for the lifetime of the app.
    private var currenLocation = MutableLiveData<DBLocation>()

    private val locationDao = MainApplication.database.locationDao()

    val locations = locationDao.getAll()

    fun setCurrentLocation(name: String, latitude: Double, longitude: Double) {
        currenLocation.postValue(DBLocation(-1, name, latitude, longitude, ""))
    }

    fun getCurrentLocation(): LiveData<DBLocation> {
        return this.currenLocation
    }

    fun insertLocation(location: DBLocation) {
        thread {
            locationDao.insertAll(location)
        }
    }

    fun deleteLocation(location: DBLocation) {
        thread {
            locationDao.delete(location)
        }
    }
}
