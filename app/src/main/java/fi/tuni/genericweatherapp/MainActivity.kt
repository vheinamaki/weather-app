package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var toolbar: Toolbar
    lateinit var changeLocation: ActivityResultLauncher<Intent>

    enum class PhotoCollection(val id: String) {
        RAIN("hlfvh66"),
        CLEAR("9ouwlqp"),
        CLOUDS("9o3bjjb"),
        STORM("bpoijxy"),
        SNOW("2k6ae11"),
        MIST("o7j0pjq")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener(this)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.title = "Tampere"
        requestWeatherAsync(61.4991, 23.7871)

        changeLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val bundle = it.data?.extras
                    if (bundle != null) {
                        toolbar.title = bundle.getString("locationName")
                        val lat = bundle.getDouble("latitude")
                        val lon = bundle.getDouble("longitude")
                        requestWeatherAsync(lat, lon)
                    }
                }
            }
    }

    fun requestLocationTitleAsync(latitude: Double, longitude: Double) {
        thread {
            val weather = OpenWeatherMap(owmKey)
            val locationName = weather.fetchLocationName(latitude, longitude)
            runOnUiThread {
                toolbar.title = locationName
            }
        }
    }

    fun requestWeatherAsync(latitude: Double, longitude: Double) {
        thread {
            val weather = OpenWeatherMap(owmKey)
            val results = weather.fetchWeather(latitude, longitude)
            runOnUiThread {
                supportFragmentManager.setFragmentResult(
                    "weatherData",
                    bundleOf(
                        "currentTemperature" to results.current.temp,
                        "currentDescription" to results.current.description,
                        "hourlyForecast" to results.hourly.toCollection(ArrayList())
                    )
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
            R.id.toolbarAddLocation -> {
                val intent = Intent(this, AddLocationActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navManageLocations -> {
                changeLocation.launch(Intent(this, LocationsActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
