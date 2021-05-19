package fi.tuni.genericweatherapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.adapters.SavedLocationAdapter
import fi.tuni.genericweatherapp.data.DBLocation
import fi.tuni.genericweatherapp.data.LocationRepository
import fi.tuni.genericweatherapp.databinding.ActivityLocationsBinding
import javax.inject.Inject

/**
 * Lists the locations the user has saved to the database.
 */
@AndroidEntryPoint
class LocationsActivity : AppCompatActivity() {

    /**
     * LocationRepository dependency to observe the database.
     */
    @Inject
    lateinit var locationRepo: LocationRepository

    /**
     * View binding for the activity.
     */
    lateinit var binding: ActivityLocationsBinding

    /**
     * RecyclerView adapter used by the location list.
     */
    private var adapter = SavedLocationAdapter()

    /**
     * A separate location item for the user's geolocation.
     */
    private lateinit var currentLocationItem: DBLocation

    /**
     * Click listener for [currentLocationItem]
     */
    private var currentLocationCallback = {
        // Set the default click listener, assuming that user's location hasn't been fetched yet
        val returnIntent = Intent()
        returnIntent.putExtra("requestLocal", true)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setup view binding
        binding = ActivityLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure the toolbar
        setSupportActionBar(binding.toolbar.root)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = resources.getString(R.string.manage_locations)

        // Configure recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        // Add a divider between the list items
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerView.adapter = adapter

        // Configure current location item
        // Set current location item's name to "Unknown", update it later when/if user's geolocation
        // is available
        currentLocationItem = DBLocation(
            -1,
            resources.getString(R.string.current_location),
            0.0,
            0.0,
            resources.getString(R.string.unknown)
        )

        // Click listener for the listed locations
        // Sends the coordinates of the selected location to MainActivity
        adapter.locationClickedListener = {
            // Special handling for current location item, trigger its own callback
            if (it.uid == -1) {
                currentLocationCallback()
            } else {
                finishWithCoordinates(it.latitude, it.longitude)
            }
        }

        // Click listener for the delete buttons
        // Show a confirmation dialog before deleting
        adapter.deleteButtonClickedListener = {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(resources.getString(R.string.confirm_delete_description, it.name))
                .setNegativeButton(R.string.confirm_delete_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.confirm_delete_ok) { dialog, _ ->
                    dialog.dismiss()
                    locationRepo.deleteLocation(it)
                }
                .show()
        }

        // Listen for changes in the database, update listed locations in recyclerView
        locationRepo.locations.observe(this) { locations ->
            // Add currentLocationItem as the first item, and locations as the rest
            val adapterItems = mutableListOf(currentLocationItem)
            adapterItems.addAll(locations)
            adapter.setItems(adapterItems)
        }

        // Listen for changes in the current geolocation
        locationRepo.getCurrentLocation().observe(this) { location ->
            // Update currentLocationItem with the new location's name
            currentLocationItem = DBLocation(
                -1,
                resources.getString(R.string.current_location),
                0.0,
                0.0,
                location.name
            )
            // Replace old currentLocationItem in the list view
            adapter.setFirst(currentLocationItem)
            // Update the callback now that the location is known
            currentLocationCallback = {
                finishWithCoordinates(location.latitude, location.longitude)
            }
        }
    }

    /**
     * Finish the activity and send the specified coordinates back to the launching activity.
     *
     * @param latitude Latitude to send back with the intent.
     * @param longitude longitude to send back with the intent.
     */
    private fun finishWithCoordinates(latitude: Double, longitude: Double) {
        val returnIntent = Intent()
        returnIntent.putExtra("latitude", latitude)
        returnIntent.putExtra("longitude", longitude)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Add toolbar_menu resource to the toolbar, containing the toolbarAddLocation button
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Click listener for the "back" arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Toolbar click events
        when (item.itemId) {
            // Back arrow clicked, finish the activity
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return true
            }
            // + button clicked
            R.id.toolbarAddLocation -> {
                // Start the location-adding activity
                startActivity(Intent(this, AddLocationActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
