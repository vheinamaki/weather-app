package fi.tuni.genericweatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.concurrent.thread

class AddLocationActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    lateinit var searchView: SearchView
    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar

    private val adapter = LocationAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Add new location"

        recyclerView = findViewById(R.id.searchRecyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        val dao = LocationDatabase.getInstance(this).locationDao()

        adapter.locationClickedListener = {
            Log.d("weatherDebug", it.toString())
            thread {
                dao.insertAll(DBLocation(it))
            }
        }

        searchView = findViewById(R.id.searchView)
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()
        searchView.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun searchLocation(query: String) {
        Log.d("weatherDebug", query)
        adapter.clear()
        thread {
            val weather = OpenWeatherMap(owmKey)
            val locations = weather.fetchCoordinates(query)
            runOnUiThread {
                locations.forEach {
                    adapter.add(it)
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchLocation(query)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?) = false
}
