package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
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
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

fun tempBitmapFromUrl(url: String): Bitmap {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connect()
    val bmp = BitmapFactory.decodeStream(connection.inputStream)
    connection.disconnect()
    return bmp
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var imageView: ImageView
    lateinit var toolbar: Toolbar
    lateinit var changeLocation: ActivityResultLauncher<Intent>

    private val adapter = HourlyWeatherAdapter(ArrayList())
    lateinit var textTemperature: TextView
    lateinit var textDescription: TextView
    lateinit var recyclerView: RecyclerView

    enum class WeatherType(val photoCollection: String) {
        RAIN("hlfvh66"),
        CLEAR("9ouwlqp"),
        CLOUDS("9o3bjjb"),
        STORM("bpoijxy"),
        SNOW("2k6ae11"),
        MIST("o7j0pjq")
    }

    fun getWeatherType(conditionCode: Int): WeatherType {
        return when(conditionCode) {
            in 200..299 -> WeatherType.STORM
            in 300..599 -> WeatherType.RAIN
            in 600..699 -> WeatherType.SNOW
            in 700..799 -> WeatherType.MIST
            in 801..899 -> WeatherType.CLOUDS
            else -> WeatherType.CLEAR
        }
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

        textTemperature = findViewById(R.id.tempTextView)
        textDescription = findViewById(R.id.descriptionTextView)
        recyclerView = findViewById(R.id.recyclerView)
        imageView = findViewById(R.id.imageView)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

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

    // Pexels api limits: 200/hour and 20,000/month
    // OWM api limits: 60/minute and 1,000,000/month

    fun requestWeatherAsync(latitude: Double, longitude: Double) {
        thread {
            val weather = OpenWeatherMap(owmKey)
            val results = weather.fetchWeather(latitude, longitude)
            val pexels = Pexels(pexelsKey)
            val photoCollection = getWeatherType(results.current.conditionCode).photoCollection
            val photoResult = pexels.fetchCollectionMedia(photoCollection)

            val photo = photoResult.random()
            val bgImage = BitmapDrawable(resources, tempBitmapFromUrl(photo.portrait))

            runOnUiThread {
                imageView.setImageDrawable(bgImage)
                // TODO: Also allow K and °F
                textTemperature.text = String.format("%.1f\u00B0C", results.current.temp)
                textDescription.text = results.current.description
                val hourly = results.hourly
                hourly.take(12).forEach {
                    adapter.add(it)
                }
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
