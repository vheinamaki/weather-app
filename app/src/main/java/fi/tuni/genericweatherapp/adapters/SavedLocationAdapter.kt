package fi.tuni.genericweatherapp.adapters

import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.genericweatherapp.data.DBLocation
import fi.tuni.genericweatherapp.R
import java.util.*

/**
 * RecyclerView adapter for locations listed in the saved locations list.
 */
class SavedLocationAdapter :
    SimpleArrayListAdapter<DBLocation, SavedLocationAdapter.Holder>(R.layout.item_location) {
    /**
     * Callback to fire when an item in the list is clicked.
     */
    var locationClickedListener: ((DBLocation) -> Unit)? = null

    /**
     * Callback to fire when the delete button of a list item is clicked
     */
    var deleteButtonClickedListener: ((DBLocation) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: RelativeLayout = view.findViewById(R.id.locationItem)
        val nameTextView: TextView = view.findViewById(R.id.locationTitleText)
        val countryTextView: TextView = view.findViewById(R.id.locationDescriptionText)
        val deleteButton: ImageButton = view.findViewById(R.id.locationDeleteButton)
    }

    override fun onBind(holder: Holder, item: DBLocation) {
        holder.nameTextView.text = item.name

        // Add click listener for the item and its delete button
        holder.item.setOnClickListener {
            locationClicked(item)
        }
        holder.deleteButton.setOnClickListener {
            deleteButtonClicked(item)
        }

        if (item.uid != -1) {
            // Set delete button visible
            holder.deleteButton.isVisible = true
            // Convert ISO alpha 2 code to country name and add it to the view
            holder.countryTextView.text =
                Locale("en", item.country).getDisplayCountry(Locale.ENGLISH)
        } else {
            // Special handling for current location (uid is -1)
            // Do not add a delete button and do not parse country name as ISO country code
            holder.deleteButton.isVisible = false
            holder.countryTextView.text = item.country
        }
    }

    override fun holderFactory(view: View) = Holder(view)

    /**
     * Called when a location item is clicked. Fires the click listener callback if it exists.
     *
     * @param location The location that was clicked
     */
    private fun locationClicked(location: DBLocation) {
        locationClickedListener?.invoke(location)
    }

    /**
     * Called when a location's delete button is clicked. Fires the delete callback if it exists.
     *
     * @param location The location whose delete button was clicked.
     */
    private fun deleteButtonClicked(location: DBLocation) {
        deleteButtonClickedListener?.invoke(location)
    }

    /**
     * Insert the item to the first index of the list.
     */
    fun setFirst(item: DBLocation) {
        if (isEmpty()) {
            add(item)
        } else {
            set(0, item)
        }
    }
}
