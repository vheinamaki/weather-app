package fi.tuni.genericweatherapp

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class LocationSearchAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Location, LocationSearchAdapter.Holder>(R.layout.item_location_searched) {
    var locationClickedListener: ((OpenWeatherMap.Location) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.locationItem)
        val nameTextView: TextView = view.findViewById(R.id.locationNameTextView)
        val countryTextView: TextView = view.findViewById(R.id.locationCountryTextView)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Location) {
        // Add click listener for the item
        holder.item.setOnClickListener {
            locationClicked(item)
        }
        holder.nameTextView.text = item.name
        holder.countryTextView.text = item.country
    }

    override fun holderFactory(view: View) = Holder(view)

    private fun locationClicked(location: OpenWeatherMap.Location) {
        locationClickedListener?.invoke(location)
    }
}

