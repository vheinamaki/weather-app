package fi.tuni.genericweatherapp

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView adapter for daily weather forecast items
 */
class DailyWeatherAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Daily, DailyWeatherAdapter.Holder>(R.layout.item_weather_daily) {
    // Format as day of the week
    val formatter = SimpleDateFormat("EEEE", Locale.ENGLISH)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Daily) {
        holder.dayTextView.text = formatter.format(item.time)
        holder.descriptionTextView.text = item.description
    }

    override fun holderFactory(view: View) = Holder(view)
}
