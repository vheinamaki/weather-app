package fi.tuni.genericweatherapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val liveWeather = MutableLiveData<WeatherRepository.WeatherPacket>()

    // TODO: Use to implement loading state
    private val loading = MutableLiveData<Boolean>()

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
            liveWeather.postValue(it)
            // Optionally send the location to LocationRepository to cache it as the current location
            if (currentLocation) {
                locationRepo.setCurrentLocation(it.locationName, latitude, longitude)
            }
        }
    }

    fun refreshForecast() {
        requestForecast(weatherRepo.latitude, weatherRepo.longitude)
    }

    fun setUnits(system: String) {
        weatherRepo.units = system
    }

    fun getUnitsSymbol(): String = when (weatherRepo.units) {
        "metric" -> "°C"
        "imperial" -> "°F"
        else -> " K"
    }
}
