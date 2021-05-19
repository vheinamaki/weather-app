package fi.tuni.genericweatherapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.adapters.LocationSearchAdapter
import fi.tuni.genericweatherapp.data.DBLocation
import fi.tuni.genericweatherapp.data.LocationRepository
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.databinding.ActivityAddLocationBinding
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * Activity for adding new locations to the saved locations list.
 */
@AndroidEntryPoint
class AddLocationActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    /**
     * LocationRepository injected as dependency, used to insert new locations into the database.
     */
    @Inject
    lateinit var locationRepo: LocationRepository

    /**
     * View binding for the activity.
     */
    lateinit var binding: ActivityAddLocationBinding

    /**
     * RecyclerView adapter for the search results list.
     */
    private val adapter = LocationSearchAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup view binding
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Loading icon initially hidden
        binding.progressIndicator.spinner.hide()

        // Configure the toolbar
        setSupportActionBar(binding.toolbar.root)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = resources.getString(R.string.add_location)

        // Configure recyclerView for listing the search results
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        // Click listener for the search results
        adapter.locationClickedListener = {
            // Add selected location to the database
            locationRepo.insertLocation(DBLocation(it))
            // Also send location information back to the launching activity
            // The result is used if the location was started from main activity, which will
            // Then make a request to its ViewModel to change the location.
            val returnIntent = Intent()
            returnIntent.putExtra("latitude", it.lat)
            returnIntent.putExtra("longitude", it.lon)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        // Set the search view as opened
        binding.searchView.setIconifiedByDefault(false)
        // Focus onto the search view, open keyboard
        binding.searchView.requestFocus()
        // Register the activity as the query listener
        binding.searchView.setOnQueryTextListener(this)
    }

    // Click listener for the back arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Make a query to the OpenWeatherMap API to receive matching locations,
     * and add them to the recyclerView via its adapter.
     *
     * @param query The query string to make the location search with.
     */
    private fun searchLocation(query: String) {
        binding.noResultsTextView.isVisible = false
        // Show the loading icon
        binding.progressIndicator.spinner.show()
        thread {
            // Make a request to geocoding API
            val weather = OpenWeatherMap(OPENWEATHERMAP_APIKEY)
            val locations = try {
                weather.fetchCoordinates(query)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                // Hide loading icon when the request finishes
                binding.progressIndicator.spinner.hide()
                if (locations != null) {
                    // Fill the adapter's list with the received locations
                    adapter.setItems(locations.toList())
                    if (locations.isEmpty()) {
                        // Show a text telling that there were no matching results
                        binding.noResultsTextView.isVisible = true
                    }
                } else {
                    // Show error message if the request fails
                    showAlert(this, R.string.request_error_title, R.string.request_error)
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        // Make a search when the user presses the submit button
        if (query != null) {
            searchLocation(query)
        }
        // Don't override default behavior, which hides the keyboard when the query is submitted
        return false
    }

    // Implementation required by the interface, but not needed by the activity
    override fun onQueryTextChange(newText: String?) = false
}
