package fi.tuni.genericweatherapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.data.WeatherRepository
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var weatherRepo: WeatherRepository

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "units") {
            val newValue = sharedPreferences.getString(key, null)
            Log.d(
                "weatherDebug",
                "Preference value was updated to: $newValue"
            )
            if (newValue != null) {
                if (weatherRepo.units != newValue) {
                    weatherRepo.informUnitsChanged()
                }
                weatherRepo.units = newValue
            }
        }
    }
}
