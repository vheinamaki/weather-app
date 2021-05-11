package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.weather.*
import kotlin.collections.ArrayList

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
    lateinit var recyclerView: RecyclerView

    // Location-changing activity launcher, used to receive the new location
    private lateinit var changeLocation: ActivityResultLauncher<Intent>

    // Adapter used by recyclerView
    private val adapter = HourlyWeatherAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain references to layout views
        drawerLayout = findViewById(R.id.drawerLayout)
        textTemperature = findViewById(R.id.tempTextView)
        textDescription = findViewById(R.id.descriptionTextView)
        textPhotographer = findViewById(R.id.photographerTextView)
        textPhotoLink = findViewById(R.id.linkTextView)
        recyclerView = findViewById(R.id.recyclerView)
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
            // TODO: Also allow K and °F
            textTemperature.text = String.format("%.1f\u00B0C", data.weather.current.temp)
            textDescription.text = data.weather.current.description
            // Insert forecast for the next 12 hours into the recyclerView via adapter
            val hourly = data.weather.hourly
            hourly.take(12).forEach {
                adapter.add(it)
            }
        }

        // Configure recyclerView's scroll direction and adapter to use
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // TODO: Replace with GPS location
        model.requestLocationChange(61.4991, 23.7871)

        // Configure the activity launcher result callback
        changeLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                // RESULT_OK is received if a location was selected in the started activity
                if (it.resultCode == Activity.RESULT_OK) {
                    // Extract new location information from the bundle
                    val bundle = it.data?.extras
                    if (bundle != null) {
                        toolbar.title = bundle.getString("locationName")
                        val lat = bundle.getDouble("latitude")
                        val lon = bundle.getDouble("longitude")
                        // Request the ViewModel to update the current location
                        model.requestLocationChange(lat, lon)
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
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
