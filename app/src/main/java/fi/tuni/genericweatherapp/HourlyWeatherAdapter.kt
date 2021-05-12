package fi.tuni.genericweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat

/**
 * RecyclerView adapter for Hourly weather forecast items
 */
class HourlyWeatherAdapter(private val data: ArrayList<OpenWeatherMap.Hourly>) :
    RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherHolder>() {
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)

    class HourlyWeatherHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_weather_hourly, parent, false)
        return HourlyWeatherHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyWeatherHolder, position: Int) {
        holder.timeTextView.text = formatter.format(data[position].time)
        holder.descriptionTextView.text = data[position].title
    }

    override fun getItemCount() = data.size

    // Add an item to the data set
    fun add(hourlyWeather: OpenWeatherMap.Hourly) {
        data.add(hourlyWeather)
        notifyDataSetChanged()
    }

    // Clear the data set
    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    // Replace the data set
    fun setItems(items: List<OpenWeatherMap.Hourly>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}
