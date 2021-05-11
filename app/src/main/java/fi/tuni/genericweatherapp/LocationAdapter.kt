package fi.tuni.genericweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class LocationAdapter(private val data: ArrayList<OpenWeatherMap.Location>) :
    RecyclerView.Adapter<LocationAdapter.LocationHolder>() {
    var locationClickedListener: ((OpenWeatherMap.Location) -> Unit)? = null

    class LocationHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.locationItem)
        val nameTextView: TextView = view.findViewById(R.id.locationNameTextView)
        val countryTextView: TextView = view.findViewById(R.id.locationCountryTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationHolder(view)
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        // Add click listener for the item
        holder.item.setOnClickListener {
            locationClicked(data[position])
        }
        holder.nameTextView.text = data[position].name
        holder.countryTextView.text = data[position].country
    }

    override fun getItemCount() = data.size

    fun add(location: OpenWeatherMap.Location) {
        data.add(location)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    private fun locationClicked(location: OpenWeatherMap.Location) {
        locationClickedListener?.invoke(location)
    }
}
