package fi.tuni.genericweatherapp.adapters

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.R
import java.util.*

/**
 * RecyclerView adapter for search results listed in the location search list.
 */
class LocationSearchAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Location, LocationSearchAdapter.Holder>(R.layout.item_location) {
    /**
     * Callback to fire when an item in the list is clicked.
     */
    var locationClickedListener: ((OpenWeatherMap.Location) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: RelativeLayout = view.findViewById(R.id.locationItem)
        val nameTextView: TextView = view.findViewById(R.id.locationTitleText)
        val countryTextView: TextView = view.findViewById(R.id.locationDescriptionText)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Location) {
        // Add click listener for the item
        holder.item.setOnClickListener {
            locationClicked(item)
        }
        holder.nameTextView.text = item.name
        // Convert ISO alpha 2 code to country name and add it to the view
        holder.countryTextView.text = Locale("en", item.country).getDisplayCountry(Locale.ENGLISH)
    }

    override fun holderFactory(view: View) = Holder(view)

    /**
     * Called when a location item is clicked. Fires the click listener callback if it exists.
     *
     * @param location The location that was clicked
     */
    private fun locationClicked(location: OpenWeatherMap.Location) {
        locationClickedListener?.invoke(location)
    }
}

