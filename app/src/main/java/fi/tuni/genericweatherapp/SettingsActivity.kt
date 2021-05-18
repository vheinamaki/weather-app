package fi.tuni.genericweatherapp

import android.app.Activity
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import dagger.hilt.android.AndroidEntryPoint
import fi.tuni.genericweatherapp.databinding.ActivityMainBinding
import fi.tuni.genericweatherapp.databinding.ActivitySettingsBinding

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure the toolbar
        setSupportActionBar(binding.toolbar.root)
        val actionBar = supportActionBar
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.title = resources.getString(R.string.settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsFragmentContainer, SettingsFragment())
            .commit()
    }

    // Click listener for the "back" arrow
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
