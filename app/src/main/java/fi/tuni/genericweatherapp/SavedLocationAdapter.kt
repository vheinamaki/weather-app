package fi.tuni.genericweatherapp

import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class SavedLocationAdapter :
    SimpleArrayListAdapter<DBLocation, SavedLocationAdapter.Holder>(R.layout.item_location) {
    var locationClickedListener: ((DBLocation) -> Unit)? = null
    var deleteButtonClickedListener: ((DBLocation) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: RelativeLayout = view.findViewById(R.id.locationItem)
        val nameTextView: TextView = view.findViewById(R.id.locationTitleText)
        val countryTextView: TextView = view.findViewById(R.id.locationDescriptionText)
        val deleteButton: ImageButton = view.findViewById(R.id.locationDeleteButton)
    }

    override fun onBind(holder: Holder, item: DBLocation) {
        // Add click listener for the item and its delete button
        holder.item.setOnClickListener {
            locationClicked(item)
        }
        holder.deleteButton.setOnClickListener {
            deleteButtonClicked(item)
        }
        holder.nameTextView.text = item.name
        // Special handling for current location
        // Do not add a delete button and do not parse country name as ISO country code
        if (item.uid != -1) {
            holder.deleteButton.isVisible = true
            holder.countryTextView.text =
                Locale("en", item.country).getDisplayCountry(Locale.ENGLISH)
        } else {
            holder.deleteButton.isVisible = false
            holder.countryTextView.text = item.country
        }
    }

    override fun holderFactory(view: View) = Holder(view)

    private fun locationClicked(location: DBLocation) {
        locationClickedListener?.invoke(location)
    }

    private fun deleteButtonClicked(location: DBLocation) {
        deleteButtonClickedListener?.invoke(location)
    }
}
