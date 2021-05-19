package fi.tuni.genericweatherapp.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.R
import fi.tuni.genericweatherapp.weatherIconToDrawableResource
import java.text.DateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * RecyclerView adapter for Hourly weather forecast items
 */
class HourlyWeatherAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Hourly, HourlyWeatherAdapter.Holder>(R.layout.item_weather_hourly) {
    /**
     * Temperature symbol to use with the view items
     */
    var symbol = ""

    /**
     * Formats date objects as HH:MM
     */
    private val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.ENGLISH)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val tempTextView: TextView = view.findViewById(R.id.tempTextView)
        val humidityTextView: TextView = view.findViewById(R.id.humidityTextView)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Hourly) {
        // Set the view's time, weather icon, humidity percentage and temperature from data object
        holder.timeTextView.text = formatter.format(item.time)
        holder.imageView.setImageResource(weatherIconToDrawableResource(item.icon))
        holder.humidityTextView.text = "${item.humidity}%"
        holder.tempTextView.text = "${item.temp.roundToInt()}$symbol"
    }

    override fun holderFactory(view: View) = Holder(view)
}
