package fi.tuni.genericweatherapp

import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * RecyclerView adapter for locations listed in the location search list
 */
class SavedLocationAdapter :
    SimpleArrayListAdapter<DBLocation, SavedLocationAdapter.Holder>(R.layout.item_location_saved) {
    var locationClickedListener: ((DBLocation) -> Unit)? = null
    var deleteButtonClickedListener: ((DBLocation) -> Unit)? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val item: LinearLayout = view.findViewById(R.id.savedLocationItem)
        val nameTextView: TextView = view.findViewById(R.id.savedLocationNameTextView)
        val countryTextView: TextView = view.findViewById(R.id.savedLocationCountryTextView)
        val deleteButton: ImageButton = view.findViewById(R.id.savedLocationDeleteButton)
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
        holder.countryTextView.text = Locale("en", item.country).getDisplayCountry(Locale.ENGLISH)
    }

    override fun holderFactory(view: View) = Holder(view)

    private fun locationClicked(location: DBLocation) {
        locationClickedListener?.invoke(location)
    }

    private fun deleteButtonClicked(location: DBLocation) {
        deleteButtonClickedListener?.invoke(location)
    }
}
