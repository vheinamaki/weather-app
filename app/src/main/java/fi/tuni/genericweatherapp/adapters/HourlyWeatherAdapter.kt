package fi.tuni.genericweatherapp.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.R
import fi.tuni.genericweatherapp.weatherIconToDrawableResource
import java.text.DateFormat
import kotlin.math.roundToInt

/**
 * RecyclerView adapter for Hourly weather forecast items
 */
class HourlyWeatherAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Hourly, HourlyWeatherAdapter.Holder>(R.layout.item_weather_hourly) {
    var symbol = ""
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val tempTextView: TextView = view.findViewById(R.id.tempTextView)
        val humidityTextView: TextView = view.findViewById(R.id.humidityTextView)
    }

    override fun onBind(holder: Holder, item: OpenWeatherMap.Hourly) {
        holder.timeTextView.text = formatter.format(item.time)
        holder.imageView.setImageResource(weatherIconToDrawableResource(item.icon))
        holder.humidityTextView.text = "${item.humidity}%"
        holder.tempTextView.text = "${item.temp.roundToInt()}$symbol"
    }

    override fun holderFactory(view: View) = Holder(view)
}
