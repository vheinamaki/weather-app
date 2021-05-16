package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.BitmapDrawable
import android.icu.util.LocaleData
import android.icu.util.ULocale
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.weather.*
import java.util.*
import javax.inject.Inject

/**
 * Application's main activity, shows a weather forecast
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var weatherRepo: WeatherRepository

    // Auto-generated view binding class (replaces findViewById calls)
    lateinit var binding: ActivityMainBinding

    // Location-changing activity launcher, used to receive the new location
    private lateinit var changeLocation: ActivityResultLauncher<Intent>

    // Adapters used by recyclerView
    private val hourlyWeatherAdapter = HourlyWeatherAdapter()
    private val dailyWeatherAdapter = DailyWeatherAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Register event listener for navigation drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Configure toolbar
        val toolbar = binding.toolbar.root
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Access the ViewModel to observe for changes in its data
        val model: WeatherViewModel by viewModels()

        // Listen for successful forecast requests
        model.getWeather().observe(this) { data ->
            // Change toolbar title to location name
            toolbar.title = data.locationName
            // Change the background image
            binding.imageView.setImageDrawable(BitmapDrawable(resources, data.bitmap))
            // Add photo credits and a link to the image
            binding.weather.photographerTextView.text = resources.getString(
                R.string.photographer_credit,
                data.photo.photographer
            )
            linkTextView.text = data.photo.pageUrl
            // Set current weather temperature and description
            // TODO: Move formatting to ViewModel
            val symbol = model.getUnitsSymbol()
            binding.weather.tempTextView.text =
                String.format("%.1f$symbol", data.weather.current.temp)
            binding.weather.descriptionTextView.text = data.weather.current.description
            // Insert forecast for the next 12 hours into the recyclerView via adapter
            val hourly = data.weather.hourly
            hourlyWeatherAdapter.setItems(hourly.take(12))
            // Insert forecast for the next 7 days
            dailyWeatherAdapter.setItems(data.weather.daily.toList())
        }

        // TODO: Display loading icon while location is being fetched
        model.isLoading().observe(this) { loading ->
            binding.weather.tempTextView.text = if (loading) "Loading..." else "Loaded"
        }

        // Listen for failed forecast requests
        model.didLoadingFail().observe(this) { didFail ->
            if (didFail) {
                Log.d("weatherDebug", "Forecast request failed")
            }
        }

        // Listen for changes in weather repository's unit settings
        weatherRepo.unitsChanged().observe(this) {
            Log.d("weatherDebug", "units changed")
            // Reload forecast with new units
            model.refreshForecast()
        }

        // Configure recyclerViews' scroll direction and adapter to use
        val horizontalManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyRecyclerView.layoutManager = horizontalManager
        hourlyRecyclerView.adapter = hourlyWeatherAdapter
        val verticalManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dailyRecyclerView.layoutManager = verticalManager
        dailyRecyclerView.adapter = dailyWeatherAdapter

        // Get current location with Fused
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        // TODO: Handle error, currently it will hang
        requestLocation(fusedClient, this) {
            val loc = it?.lastLocation
            if (loc != null) {
                Log.d("weatherDebug", loc.toString())
                // Request weather for the received coordinates
                model.requestForecast(loc.latitude, loc.longitude, currentLocation = true)
            } else {
                Log.d("weatherDebug", "Location request failed")
            }
        }

        // Configure the activity launcher result callback
        // Used by LocationsActivity and AddLocationActivity to send back the selected location
        changeLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // RESULT_OK is received if a location was selected in the started activity
                if (it.resultCode == Activity.RESULT_OK) {
                    // Extract new location information from the bundle
                    val bundle = it.data?.extras
                    if (bundle != null) {
                        val lat = bundle.getDouble("latitude")
                        val lon = bundle.getDouble("longitude")
                        // Request the ViewModel to update the current location
                        model.requestForecast(lat, lon)
                    }
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Add toolbar_menu resource to the toolbar, containing the toolbarAddLocation button
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Toolbar click events
        when (item.itemId) {
            android.R.id.home -> {
                binding.root.openDrawer(GravityCompat.START)
                return true
            }
            R.id.toolbarAddLocation -> {
                // Start the location-adding activity and trigger the result callback when done
                changeLocation.launch(Intent(this, AddLocationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Navigation drawer click events
        when (item.itemId) {
            R.id.navManageLocations -> {
                changeLocation.launch(Intent(this, LocationsActivity::class.java))
            }
            R.id.navSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        binding.root.closeDrawer(GravityCompat.START)
        return true
    }
}
