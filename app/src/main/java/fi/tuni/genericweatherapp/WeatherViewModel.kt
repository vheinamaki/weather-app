package fi.tuni.genericweatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val repository: WeatherRepository) :
    ViewModel() {
    private val liveWeather: MutableLiveData<WeatherRepository.WeatherPacket> by lazy {
        MutableLiveData<WeatherRepository.WeatherPacket>().also {
            // Call loadWeather when liveWeather property is accessed for the first time
            loadWeather()
        }
    }
    private val loading = MutableLiveData<Boolean>()

    // Exposes immutable version of the live data to the observing activity
    fun getWeather(): LiveData<WeatherRepository.WeatherPacket> {
        return liveWeather
    }

    private fun loadWeather() {
        loading.value = true
        repository.fetchWeatherAsync {
            // Use postValue instead of setting directly, since the lambda is run on a worker thread
            loading.postValue(false)
            liveWeather.postValue(it)
        }
    }

    fun requestLocationChange(latitude: Double, longitude: Double) {
        repository.changeLocation(latitude, longitude)
        loadWeather()
    }
}
