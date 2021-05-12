package fi.tuni.genericweatherapp

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
class WeatherViewModel @Inject constructor(private val repository: WeatherRepository) :
    ViewModel() {
    private val liveWeather = MutableLiveData<WeatherRepository.WeatherPacket>()

    // TODO: Use to implement loading state
    private val loading = MutableLiveData<Boolean>()

    // Exposes immutable version of the live data to the observing activity
    fun getWeather(): LiveData<WeatherRepository.WeatherPacket> {
        return liveWeather
    }

    // Get data from WeatherRepository
    private fun loadWeather() {
        loading.value = true
        repository.fetchWeatherAsync {
            // Use postValue instead of setting directly, since the lambda is run on a worker thread
            loading.postValue(false)
            liveWeather.postValue(it)
        }
    }

    // Forward location change request to WeatherRepository and then update
    fun requestForecast(latitude: Double, longitude: Double) {
        repository.changeLocation(latitude, longitude)
        loadWeather()
    }
}
