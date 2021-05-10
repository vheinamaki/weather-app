package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.concurrent.thread

class LocationsActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var recyclerView: RecyclerView
    private var adapter = LocationAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerView)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        adapter.locationClickedListener = {
            val returnIntent = Intent()
            returnIntent.putExtra("locationName", it.name)
            returnIntent.putExtra("latitude", it.lat)
            returnIntent.putExtra("longitude", it.lon)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        val dao = LocationDatabase.getInstance(this).locationDao()

        thread {
            val locations = dao.getAll()
            runOnUiThread {
                locations.forEach {
                    Log.d("weatherDebug", OpenWeatherMap.Location(it).toString())
                    adapter.add(OpenWeatherMap.Location(it))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
