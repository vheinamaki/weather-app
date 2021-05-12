package fi.tuni.genericweatherapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.concurrent.thread

/**
 * Lists the locations the user has saved to the database
 */
@AndroidEntryPoint
class LocationsActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar
    lateinit var recyclerView: RecyclerView
    private var adapter = SavedLocationAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        // Configure the toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        // Configure recyclerView
        recyclerView = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        // Click listener for the listed locations
        // Sends the name and the coordinates of the selected location to MainActivity
        adapter.locationClickedListener = {
            val returnIntent = Intent()
            returnIntent.putExtra("locationName", it.name)
            returnIntent.putExtra("latitude", it.latitude)
            returnIntent.putExtra("longitude", it.longitude)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        // Data access object for querying the database
        val dao = MainApplication.database.locationDao()

        adapter.deleteButtonClickedListener = {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(resources.getString(R.string.confirm_delete_description, it.name))
                .setNegativeButton(R.string.confirm_delete_cancel) {
                    dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.confirm_delete_ok) {
                    dialog, _ ->
                    dialog.dismiss()
                    thread {
                        dao.delete(it)
                    }
                }
                .show()
        }

        // Listen for changes in the database, update listed locations in recyclerView
        dao.getAll().observe(this) { locations ->
            adapter.clear()
            locations.forEach {
                adapter.add(it)
            }
        }
    }

    // Click listener for the "back" arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
