package fi.tuni.genericweatherapp

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var textTemperature: TextView
    lateinit var textDescription: TextView
    lateinit var recyclerView: RecyclerView

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
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        textTemperature = findViewById(R.id.tempTextView)
        textDescription = findViewById(R.id.descriptionTextView)

        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        val adapter = HourlyWeatherAdapter(ArrayList())
        recyclerView.adapter = adapter

        thread {
            val weather = OpenWeatherMap(owmKey)
            val results = weather.fetchWeather(61.4991, 23.7871)
            val locationName = weather.fetchLocationName(61.4991, 23.7871)
            Log.d("weatherDebug", locationName)
            Log.d("weatherDebug", results.current.title)
            Log.d("weatherDebug", results.current.temp.toString())
            runOnUiThread {
                // TODO: Also allow K and °F
                textTemperature.text = String.format("%.1f\u00B0C", results.current.temp)
                textDescription.text = results.current.description
                toolbar.title = locationName
                results.hourly.take(12).forEach {
                    adapter.add(it)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
