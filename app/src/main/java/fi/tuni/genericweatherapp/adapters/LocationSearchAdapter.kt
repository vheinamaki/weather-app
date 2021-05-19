package fi.tuni.genericweatherapp.adapters

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.R
import java.util.*

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class LocationSearchAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Location, LocationSearchAdapter.Holder>(R.layout.item_location) {
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
        holder.countryTextView.text = Locale("en", item.country).getDisplayCountry(Locale.ENGLISH)
    }

    override fun holderFactory(view: View) = Holder(view)

    private fun locationClicked(location: OpenWeatherMap.Location) {
        locationClickedListener?.invoke(location)
    }
}

