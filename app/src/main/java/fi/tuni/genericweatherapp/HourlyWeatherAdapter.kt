package fi.tuni.genericweatherapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DateFormat

class HourlyWeatherAdapter(private val data: ArrayList<OpenWeatherMap.Hourly>) : RecyclerView.Adapter<HourlyWeatherAdapter.HourlyWeatherHolder>() {
    val formatter = DateFormat.getTimeInstance(DateFormat.SHORT)

    class HourlyWeatherHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_hourly, parent, false)
        return HourlyWeatherHolder(view)
    }

    override fun onBindViewHolder(holder: HourlyWeatherHolder, position: Int) {
        holder.timeTextView.text = formatter.format(data[position].time)
        holder.descriptionTextView.text = data[position].title
    }

    override fun getItemCount() = data.size

    // Add persons to list
    fun add(person: OpenWeatherMap.Hourly) {
        data.add(person)
        notifyDataSetChanged()
    }
}
