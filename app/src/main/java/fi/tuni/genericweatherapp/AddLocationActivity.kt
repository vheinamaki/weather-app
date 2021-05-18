package fi.tuni.genericweatherapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.databinding.ActivityAddLocationBinding
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * Activity for adding new locations to the saved locations list
 */
@AndroidEntryPoint
class AddLocationActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    @Inject
    lateinit var locationRepo: LocationRepository

    lateinit var binding: ActivityAddLocationBinding

    private val adapter = LocationSearchAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Progress indicator initially hidden
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
            returnIntent.putExtra("locationName", it.name)
            returnIntent.putExtra("latitude", it.lat)
            returnIntent.putExtra("longitude", it.lon)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.requestFocus()
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

    // Make a query to the OpenWeatherMap API to receive matching locations,
    // and add them to the recyclerView via its adapter
    private fun searchLocation(query: String) {
        binding.noResultsTextView.isVisible = false
        binding.progressIndicator.spinner.show()
        thread {
            val weather = OpenWeatherMap(owmKey)
            val locations = try {
                weather.fetchCoordinates(query)
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                binding.progressIndicator.spinner.hide()
                if (locations != null) {
                    adapter.setItems(locations.toList())
                    if (locations.isEmpty()) {
                        binding.noResultsTextView.isVisible = true
                    }
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.request_error_title)
                        .setMessage(R.string.request_error)
                        .setNeutralButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
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
