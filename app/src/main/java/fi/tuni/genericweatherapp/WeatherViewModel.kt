package fi.tuni.genericweatherapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import fi.tuni.genericweatherapp.data.LocationRepository
import fi.tuni.genericweatherapp.data.WeatherRepository
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for managing MainActivity's data.
 *
 * Responsible for serving weather data to MainActivity and forwarding location change requests to
 * WeatherRepository.
 *
 * @property weatherRepo injected by Hilt as a dependency. Used to Request forecasts.
 * @property locationRepo injected by Hilt as a dependency. Used to send the user's current
 * geolocation to the LocationRepository so that other views can access it.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val locationRepo: LocationRepository, application: Application
) :
    AndroidViewModel(application) {

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        // Set weatherRepo's temperature units from preferences, or depending on the default locale
        // if the preference has not been set.
        val defaultUnits =
            if (Locale.getDefault().usesFahrenheits()) "imperial" else "metric"
        val unitsPref = preferences.getString("units", defaultUnits) ?: defaultUnits
        val usedUnits =
            if (unitsPref in listOf(
                    "metric",
                    "imperial",
                    "standard"
                )
            ) unitsPref else defaultUnits.also {
                // Save the units in preferences as well
                preferences.edit().putString("units", defaultUnits).apply()
            }
        weatherRepo.units = usedUnits

        // Set initial live coordinate value to null, which makes MainActivity request a forecast
        // for local coordinates.
        weatherRepo.liveCoordinates.value = null
    }

    /**
     * LiveData which gets updated with forecasts as they are requested.
     */
    private val liveWeather = MutableLiveData<WeatherRepository.WeatherPacket>()

    /**
     * Exposes immutable version of [liveWeather] to observers.
     */
    fun getWeather(): LiveData<WeatherRepository.WeatherPacket> = liveWeather

    /**
     * Updated with true and false when forecast fetching begins and ends.
     */
    private val loading = MutableLiveData<Boolean>()

    /**
     * Exposes immutable version of [loading]. Used to determine whether a loading icon
     * should be shown.
     */
    fun isLoading(): LiveData<Boolean> = loading

    /**
     * Updated with true when there is an error fetching a forecast,
     * and false when fetching succeeds.
     */
    private val loadingError = MutableLiveData<Boolean>()

    /**
     * Exposes immutable version of [loadingError]. Used to display an error message
     * when request fails.
     */
    fun didLoadingFail(): LiveData<Boolean> = loadingError

    /**
     * Make a forecast request with the given latitude and longitude. The forecast is posted to
     * the [liveWeather] object when done.
     *
     * @param latitude Latitude of the location.
     * @param longitude Longitude of the location.
     * @param currentLocation whether or not to cache this as the user's geolocation, which
     * can be then shown in the saved locations list as the current location.
     */
    fun requestForecast(latitude: Double, longitude: Double, currentLocation: Boolean = false) {
        loading.value = true
        weatherRepo.fetchWeatherAsync(latitude, longitude) {
            // Use postValue instead of setting directly, since the lambda is run on a worker thread
            loading.postValue(false)
            if (it != null) {
                loadingError.postValue(false)
                liveWeather.postValue(it)
                // Optionally send the location to LocationRepository to cache it as the current location
                if (currentLocation) {
                    locationRepo.setCurrentLocation(it.locationName, latitude, longitude)
                }
            } else {
                loadingError.postValue(true)
            }
        }
    }

    /**
     * Make a forecast request with the same location as previously.
     */
    fun refreshForecast() {
        requestForecast(weatherRepo.previousLatitude, weatherRepo.previousLongitude)
    }

    /**
     * @return Temperature symbol for the units currently used by [weatherRepo].
     */
    fun getUnitsSymbol(): String = when (weatherRepo.units) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> " K"
    }
}
