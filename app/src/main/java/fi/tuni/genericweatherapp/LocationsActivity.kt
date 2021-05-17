package fi.tuni.genericweatherapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.databinding.ActivityLocationsBinding
import fi.tuni.genericweatherapp.databinding.ActivityMainBinding
import javax.inject.Inject
import kotlin.concurrent.thread

/**
 * Lists the locations the user has saved to the database
 */
@AndroidEntryPoint
class LocationsActivity : AppCompatActivity() {
    @Inject
    lateinit var locationRepo: LocationRepository

    lateinit var binding: ActivityLocationsBinding

    private var adapter = SavedLocationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure the toolbar
        setSupportActionBar(binding.toolbar.root)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Configure recyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter

        // Configure current location button
        // Set current location item's text to "Current location (Unknown)", update it later
        binding.currentLocationItem.text =
            resources.getString(R.string.current_location, resources.getString(R.string.unknown))
        // Set the default click listener, assuming that user's location hasn't been fetched yet
        binding.currentLocationItem.setOnClickListener {
            val returnIntent = Intent()
            returnIntent.putExtra("requestLocal", true)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        // Click listener for the listed locations
        // Sends the name and the coordinates of the selected location to MainActivity
        adapter.locationClickedListener = {
            finishWithCoordinates(it.latitude, it.longitude)
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
            adapter.setItems(locations)
        }

        // Listen for changes in the current GPS location
        locationRepo.getCurrentLocation().observe(this) { location ->
            Log.d("weatherDebug", "current GPS location: $location")
            binding.currentLocationItem.text =
                resources.getString(R.string.current_location, location.name)
            binding.currentLocationItem.setOnClickListener {
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
