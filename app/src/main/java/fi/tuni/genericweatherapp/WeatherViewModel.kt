package fi.tuni.genericweatherapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for managing MainActivity's data.
 *
 * Responsible for serving weather data to MainActivity and forwarding location change requests to
 * WeatherRepository.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepo: WeatherRepository,
    private val locationRepo: LocationRepository, application: Application
) :
    AndroidViewModel(application) {

    init {
        Log.d("weatherDebug", "ViewModel init")
        val preferences = PreferenceManager.getDefaultSharedPreferences(getApplication())

        // Set temperature units
        val fahrenheitUsers = listOf(
            "us", // U.S.
            "bs", // Bahamas
            "ky", // Cayman Islands
            "lr", // Liberia
            "pw", // Palau
            "fm", // Micronesia
            "mh", // Marshall Islands
        )

        val defaultUnits =
            if (Locale.getDefault().country in fahrenheitUsers) "imperial" else "metric"
        val unitsPref = preferences.getString("units", defaultUnits) ?: defaultUnits
        val usedUnits =
            if (unitsPref in listOf(
                    "metric",
                    "imperial",
                    "standard"
                )
            ) unitsPref else defaultUnits.also {
                preferences.edit().putString("units", defaultUnits).apply()
            }
        weatherRepo.units = usedUnits

        // Get the last viewed location and request forecast for it, or for local coordinates
    }

    private val liveWeather = MutableLiveData<WeatherRepository.WeatherPacket>()

    private val loading = MutableLiveData<Boolean>()

    private val loadingError = MutableLiveData<Boolean>()

    fun isLoading(): LiveData<Boolean> = loading

    fun didLoadingFail(): LiveData<Boolean> = loadingError

    // Exposes immutable version of the live data to the observing activity
    fun getWeather(): LiveData<WeatherRepository.WeatherPacket> {
        return liveWeather
    }

    // Forward location change request to WeatherRepository and then update
    // currentLocation determines whether or not to cache this as the user's GPS location, which
    // can be then shown in the location list as the current location
    fun requestForecast(latitude: Double, longitude: Double, currentLocation: Boolean = false) {
        weatherRepo.changeLocation(latitude, longitude)
        loading.value = true
        weatherRepo.fetchWeatherAsync {
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

    fun refreshForecast() {
        requestForecast(weatherRepo.latitude, weatherRepo.longitude)
    }

    fun getUnitsSymbol(): String = when (weatherRepo.units) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> " K"
    }
}
