package fi.tuni.genericweatherapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.concurrent.thread

/**
 * Activity for adding new locations to the saved locations list
 */
class AddLocationActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    lateinit var searchView: SearchView
    lateinit var recyclerView: RecyclerView
    lateinit var toolbar: Toolbar

    private val adapter = LocationSearchAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        // Configure the toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = "Add new location"

        // Configure recyclerView for listing the search results
        recyclerView = findViewById(R.id.searchRecyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // Data access object for making queries to the database
        val dao = MainApplication.database.locationDao()

        // Click listener for the search results
        adapter.locationClickedListener = {
            // Add selected location to the database
            thread {
                dao.insertAll(DBLocation(it))
            }
            // Also send location information back to the launching activity
            // The result is used if the location was started from main activity, which will
            // Then make a request to its ViewModel to change the location.
            val returnIntent = Intent()
            returnIntent.putExtra("locationName", it.name)
            returnIntent.putExtra("latitude", it.lat)
            returnIntent.putExtra("longitude", it.lon)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        searchView = findViewById(R.id.searchView)
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()
        searchView.setOnQueryTextListener(this)
    }

    // Click listener for the back arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    // Make a query to the OpenWeatherMap API to receive matching locations,
    // and add them to the recyclerView via its adapter
    private fun searchLocation(query: String) {
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

    // Make a search when the user pressed the submit button
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchLocation(query)
        }
        // Don't override default behavior, which hides the keyboard when the query is submitted
        return false
    }

    override fun onQueryTextChange(newText: String?) = false
}
