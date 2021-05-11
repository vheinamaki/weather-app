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

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var imageView: ImageView
    lateinit var toolbar: Toolbar
    lateinit var changeLocation: ActivityResultLauncher<Intent>

    private val adapter = HourlyWeatherAdapter(ArrayList())
    lateinit var textTemperature: TextView
    lateinit var textDescription: TextView
    lateinit var textPhotographer: TextView
    lateinit var textPhotoLink: TextView
    lateinit var recyclerView: RecyclerView

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
        textPhotographer = findViewById(R.id.photographerTextView)
        textPhotoLink = findViewById(R.id.linkTextView)
        recyclerView = findViewById(R.id.recyclerView)
        imageView = findViewById(R.id.imageView)

        val model: WeatherViewModel by viewModels()
        model.getWeather().observe(this) { data ->
            imageView.setImageDrawable(BitmapDrawable(resources, data.bitmap))
            textPhotographer.text = resources.getString(
                R.string.photographer_credit,
                data.photo.photographer
            )
            linkTextView.text = data.photo.pageUrl
            // TODO: Also allow K and °F
            textTemperature.text = String.format("%.1f\u00B0C", data.weather.current.temp)
            textDescription.text = data.weather.current.description
            val hourly = data.weather.hourly
            hourly.take(12).forEach {
                adapter.add(it)
            }
        }

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        model.requestLocationChange(61.4991, 23.7871)

        changeLocation =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val bundle = it.data?.extras
                    if (bundle != null) {
                        toolbar.title = bundle.getString("locationName")
                        val lat = bundle.getDouble("latitude")
                        val lon = bundle.getDouble("longitude")
                        model.requestLocationChange(lat, lon)
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
