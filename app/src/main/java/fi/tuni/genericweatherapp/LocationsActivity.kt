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
 * Lists the locations the user has saved to the database
 */
@AndroidEntryPoint
class LocationsActivity : AppCompatActivity() {
    @Inject
    lateinit var locationRepo: LocationRepository

    lateinit var binding: ActivityLocationsBinding

    private var adapter = SavedLocationAdapter()

    private lateinit var currentLocationItem: DBLocation

    // Set the default click listener, assuming that user's location hasn't been fetched yet
    private var currentLocationCallback = {
        val returnIntent = Intent()
        returnIntent.putExtra("requestLocal", true)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.recyclerView.adapter = adapter

        // Configure current location item
        // Set current location item's name to "Unknown", update it later
        currentLocationItem = DBLocation(
            -1,
            resources.getString(R.string.current_location),
            0.0,
            0.0,
            resources.getString(R.string.unknown)
        )

        // Click listener for the listed locations
        // Sends the name and the coordinates of the selected location to MainActivity
        adapter.locationClickedListener = {
            // Special handling for current location with unknown coordinates
            if (it.uid == -1) {
                currentLocationCallback()
            } else {
                finishWithCoordinates(it.latitude, it.longitude)
            }
        }

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

        // Listen for changes in the current GPS location
        locationRepo.getCurrentLocation().observe(this) { location ->
            Log.d("weatherDebug", "current GPS location: ${location.name}")
            currentLocationItem = DBLocation(
                -1,
                resources.getString(R.string.current_location),
                0.0,
                0.0,
                location.name
            )
            adapter.set(0, currentLocationItem)
            currentLocationCallback = {
                finishWithCoordinates(location.latitude, location.longitude)
            }
        }
    }

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
            android.R.id.home -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
                return true
            }
            R.id.toolbarAddLocation -> {
                // Start the location-adding activity
                startActivity(Intent(this, AddLocationActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
