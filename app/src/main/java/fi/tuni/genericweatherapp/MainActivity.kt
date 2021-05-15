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
import kotlinx.android.synthetic.main.weather.*
import java.util.*

/**
 * Application's main activity, shows a weather forecast
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var imageView: ImageView
    lateinit var toolbar: Toolbar
    lateinit var textTemperature: TextView
    lateinit var textDescription: TextView
    lateinit var textPhotographer: TextView
    lateinit var textPhotoLink: TextView
    lateinit var hourlyRecyclerView: RecyclerView
    lateinit var dailyRecyclerView: RecyclerView

    // Location-changing activity launcher, used to receive the new location
    private lateinit var changeLocation: ActivityResultLauncher<Intent>

    // Adapters used by recyclerView
    private val hourlyWeatherAdapter = HourlyWeatherAdapter()
    private val dailyWeatherAdapter = DailyWeatherAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain references to layout views
        drawerLayout = findViewById(R.id.drawerLayout)
        textTemperature = findViewById(R.id.tempTextView)
        textDescription = findViewById(R.id.descriptionTextView)
        textPhotographer = findViewById(R.id.photographerTextView)
        textPhotoLink = findViewById(R.id.linkTextView)
        hourlyRecyclerView = findViewById(R.id.hourlyRecyclerView)
        dailyRecyclerView = findViewById(R.id.dailyRecyclerView)
        imageView = findViewById(R.id.imageView)

        navigationView = findViewById(R.id.navigationView)
        // Register event listener for navigation drawer
        navigationView.setNavigationItemSelectedListener(this)

        // Configure toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Access the ViewModel and observe for changes in its weather data
        val model: WeatherViewModel by viewModels()
        model.getWeather().observe(this) { data ->
            // Change toolbar title to location name
            toolbar.title = data.locationName
            // Change the background image
            imageView.setImageDrawable(BitmapDrawable(resources, data.bitmap))
            // Add photo credits and a link to the image
            textPhotographer.text = resources.getString(
                R.string.photographer_credit,
                data.photo.photographer
            )
            linkTextView.text = data.photo.pageUrl
            // Set current weather temperature and description
            // TODO: Move formatting to ViewModel
            val symbol = model.getUnitsSymbol()
            textTemperature.text = String.format("%.1f$symbol", data.weather.current.temp)
            textDescription.text = data.weather.current.description
            // Insert forecast for the next 12 hours into the recyclerView via adapter
            val hourly = data.weather.hourly
            hourlyWeatherAdapter.setItems(hourly.take(12))
            // Insert forecast for the next 7 days
            dailyWeatherAdapter.setItems(data.weather.daily.toList())
        }

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

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
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
        model.setUnits(usedUnits)


        // Configure recyclerViews' scroll direction and adapter to use
        val horizontalManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        hourlyRecyclerView.layoutManager = horizontalManager
        hourlyRecyclerView.adapter = hourlyWeatherAdapter
        val verticalManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dailyRecyclerView.layoutManager = verticalManager
        dailyRecyclerView.adapter = dailyWeatherAdapter

        // Get current location with Fused
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        // TODO: Display loading icon while location is being fetched
        // TODO: Handle error, currently it will hang
        requestLocation(fusedClient, this) {
            val loc = it.lastLocation
            Log.d("weatherDebug", loc.toString())
            // Request weather for the received coordinates
            model.requestForecast(loc.latitude, loc.longitude, currentLocation = true)
        }

        // Configure the activity launcher result callback
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
                drawerLayout.openDrawer(GravityCompat.START)
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
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
