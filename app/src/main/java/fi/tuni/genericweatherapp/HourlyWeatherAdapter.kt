package fi.tuni.genericweatherapp

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat

/**
 * RecyclerView adapter for Hourly weather forecast items
 */
class HourlyWeatherAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Hourly, HourlyWeatherAdapter.Holder>(R.layout.item_weather_hourly) {
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Hourly) {
        holder.timeTextView.text = formatter.format(item.time)
        holder.descriptionTextView.text = item.title
    }

    override fun holderFactory(view: View) = Holder(view)
}
