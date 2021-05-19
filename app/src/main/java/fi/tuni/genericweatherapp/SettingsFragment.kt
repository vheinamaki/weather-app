package fi.tuni.genericweatherapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.data.WeatherRepository
import javax.inject.Inject

/**
 * Fragment that shows the app's settings, and notifies weather repository of made changes.
 */
@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * WeatherRepository dependency, gets notified of changes made to preferences.
     */
    @Inject
    lateinit var weatherRepo: WeatherRepository


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Set the settings resource to show
        setPreferencesFromResource(R.xml.settings, rootKey)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    // Listen for changes made to preferences
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "units") {
            val newValue = sharedPreferences.getString(key, null)
            if (newValue != null) {
                // Set the new units in weather repository, so that they will be used for the
                // following forecast requests
                if (weatherRepo.units != newValue) {
                    weatherRepo.informUnitsChanged()
                }
                weatherRepo.units = newValue
            }
        }
    }
}
