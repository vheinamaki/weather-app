package fi.tuni.genericweatherapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
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
import fi.tuni.genericweatherapp.databinding.ActivityMainBinding
import javax.inject.Inject

/**
 * Application's main activity, shows a weather forecast
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject
    lateinit var weatherRepo: WeatherRepository

    // Access the ViewModel to observe for changes in its data
    private val model: WeatherViewModel by viewModels()

    // Auto-generated view binding class (replaces findViewById calls)
    lateinit var binding: ActivityMainBinding

    // Location-changing activity launcher, used to receive the new location
    // Used by LocationsActivity and AddLocationActivity to send back the selected location
    private val changeLocation =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // RESULT_OK is received if a location was selected in the started activity
            if (it.resultCode == Activity.RESULT_OK) {
                // Extract new location information from the bundle
                val bundle = it.data?.extras
                if (bundle != null) {
                    // Request the ViewModel to update the current location
                    val localRequest = bundle.getBoolean("requestLocal", false)
                    if (localRequest) {
                        requestLocalForecast()
                    } else {
                        val lat = bundle.getDouble("latitude")
                        val lon = bundle.getDouble("longitude")
                        model.requestForecast(lat, lon)
                    }
                }
            }
        }

    private val askLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, make request
            makeLocationRequest()
        } else {
            // Rejected
            Log.d("weatherDebug", "Location permission rejected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressIndicator.spinner.hide()

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
                Log.d("weatherDebug", "Forecast request failed")
                // Ask Splash Screen to show retry button
                supportFragmentManager.setFragmentResult(
                    "showButtons",
                    bundleOf("retryButton" to true)
                )
                showAlert(this, R.string.request_error_title, R.string.request_error)
            }
        }

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
            Log.d("weatherDebug", "units changed")
            // Reload forecast with new units
            model.refreshForecast()
        }

        // TODO: Move control to view model
        requestLocalForecast()
    }

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
                Log.d("weatherDebug", "Location request failed")
                showAlert(this, R.string.location_unavailable_title, R.string.location_unavailable)
            }
        }
    }

    private fun requestLocalForecast() {
        val perm = Manifest.permission.ACCESS_FINE_LOCATION
        val alreadyGranted =
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        // Check location usage permissions
        if (alreadyGranted) {
            // Permission has already been granted, make request
            makeLocationRequest()
        } else {
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
            android.R.id.home -> {
                binding.root.openDrawer(GravityCompat.START)
                return true
            }
            R.id.toolbarAddLocation -> {
                // Start the location-adding activity and trigger the result callback when done
                changeLocation.launch(Intent(this, AddLocationActivity::class.java))
                return true
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
            R.id.navAddLocation -> {
                changeLocation.launch(Intent(this, AddLocationActivity::class.java))
            }
        }
        binding.root.closeDrawer(GravityCompat.START)
        return true
    }
}
