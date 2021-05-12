package fi.tuni.genericweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class SavedLocationAdapter(private val data: ArrayList<DBLocation>) :
    RecyclerView.Adapter<SavedLocationAdapter.Holder>() {
    var locationClickedListener: ((DBLocation) -> Unit)? = null
    var deleteButtonClickedListener: ((DBLocation) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.savedLocationItem)
        val nameTextView: TextView = view.findViewById(R.id.savedLocationNameTextView)
        val countryTextView: TextView = view.findViewById(R.id.savedLocationCountryTextView)
        val deleteButton: ImageButton = view.findViewById(R.id.savedLocationDeleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_location_saved, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // Add click listener for the item
        holder.item.setOnClickListener {
            locationClicked(data[position])
        }
        holder.deleteButton.setOnClickListener {
            deleteButtonClicked(data[position])
        }
        holder.nameTextView.text = data[position].name
        holder.countryTextView.text = data[position].country
    }

    override fun getItemCount() = data.size

    fun add(location: DBLocation) {
        data.add(location)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    private fun locationClicked(location: DBLocation) {
        locationClickedListener?.invoke(location)
    }

    private fun deleteButtonClicked(location: DBLocation) {
        deleteButtonClickedListener?.invoke(location)
    }
}
