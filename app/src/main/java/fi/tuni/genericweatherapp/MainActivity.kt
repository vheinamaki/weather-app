package fi.tuni.genericweatherapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.data.WeatherRepository
import fi.tuni.genericweatherapp.databinding.ActivityMainBinding
import javax.inject.Inject

/**
 * Application's main activity, displays initially [SplashScreenFragment] and then
 * [WeatherFragment] when the forecast has been fetched.
 *
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    /**
     *  WeatherRepository injected by Hilt for observing its LiveData.
     */
    @Inject
    lateinit var weatherRepo: WeatherRepository

    /**
     * The Activity's ViewModel, used to observe changes in its data.
     */
    private val model: WeatherViewModel by viewModels()

    /**
     * Auto-generated view binding class (replaces findViewById calls).
     */
    lateinit var binding: ActivityMainBinding

    /**
     * Activity launcher for asking geolocation permission.
     */
    private val askLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, make request
            makeLocationRequest()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the loading icon by default
        binding.progressIndicator.spinner.hide()

        // Show the splash screen at start
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.mainFragment, SplashScreenFragment::class.java, null)
        }

        // Register event listener for navigation drawer
        binding.navigationView.setNavigationItemSelectedListener(this)

        // Configure toolbar
        val toolbar = binding.toolbar.root
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Show loading screen when loading begins
        model.isLoading().observe(this) { loadingBegun ->
            if (loadingBegun) {
                binding.progressIndicator.spinner.show()
            } else {
                binding.progressIndicator.spinner.hide()
            }
        }

        // Listen for failed forecast requests
        model.didLoadingFail().observe(this) { didFail ->
            if (didFail) {
                // Ask Splash Screen to show retry button
                supportFragmentManager.setFragmentResult(
                    "showButtons",
                    bundleOf("retryButton" to true)
                )
                showAlert(this, R.string.request_error_title, R.string.request_error)
            }
        }

        // Switch the fragment to WeatherFragment when the forecast request has finished
        model.getWeather().observe(this) { data ->
            // Change toolbar title to location name
            toolbar.title = data.locationName
            // Show forecast
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.mainFragment, WeatherFragment::class.java, null)
            }
        }

        // Listen for changes in weather repository's unit settings
        weatherRepo.unitsChanged().observe(this) {
            // Reload forecast with new units
            model.refreshForecast()
        }

        // Listen for changes in coordinates, make a request for the location when they change
        weatherRepo.liveCoordinates.observe(this) { coords ->
            if (coords == null) {
                requestLocalForecast()
            } else {
                model.requestForecast(coords.first, coords.second)
            }
        }
    }

    /**
     * Make a geolocation request, and a forecast request for the received coordinates
     * if it succeeds. Otherwise show an alert dialog with an error message.
     */
    private fun makeLocationRequest() {
        // Get current location with Fused
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        binding.progressIndicator.spinner.show()
        requestLocation(fusedClient) { result ->
            binding.progressIndicator.spinner.hide()
            val loc = result?.lastLocation
            if (loc != null) {
                // Request weather for the received coordinates
                model.requestForecast(loc.latitude, loc.longitude, currentLocation = true)
            } else {
                // Request timed out
                showAlert(this, R.string.location_unavailable_title, R.string.location_unavailable)
            }
        }
    }

    /**
     * Ask for location permissions and call [makeLocationRequest] if they were granted.
     */
    private fun requestLocalForecast() {
        val perm = Manifest.permission.ACCESS_FINE_LOCATION
        val alreadyGranted =
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        // Check location usage permissions
        if (alreadyGranted) {
            // Permission has already been granted, make request
            makeLocationRequest()
        } else {
            // Launch the permission asking activity
            askLocationPermission.launch(perm)
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
            // Navigation drawer button clicked, open the drawer
            android.R.id.home -> {
                binding.root.openDrawer(GravityCompat.START)
                return true
            }
            // + button clicked
            R.id.toolbarAddLocation -> {
                // Start the location-adding activity
                startActivity(Intent(this, AddLocationActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Navigation drawer click events
        when (item.itemId) {
            // 'Manage Locations' option
            R.id.navManageLocations -> {
                startActivity(Intent(this, LocationsActivity::class.java))
            }
            // Settings option
            R.id.navSettings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            // 'Add Location' option
            R.id.navAddLocation -> {
                startActivity(Intent(this, AddLocationActivity::class.java))
            }
        }
        // Close the navigation drawer when an item was selected
        binding.root.closeDrawer(GravityCompat.START)
        return true
    }
}
