package fi.tuni.genericweatherapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WeatherFragment : Fragment(R.layout.fragment_weather) {
    private val adapter = HourlyWeatherAdapter(ArrayList())
    lateinit var textTemperature: TextView
    lateinit var textDescription: TextView
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener("weatherData") {
            requestKey, bundle ->
            // TODO: Also allow K and °F
            textTemperature.text = String.format("%.1f\u00B0C", bundle.getDouble("currentTemperature"))
            textDescription.text = bundle.getString("currentDescription")
            val hourly = bundle.getParcelableArrayList<OpenWeatherMap.Hourly>("hourlyForecast")
            hourly?.take(12)?.forEach {
                adapter.add(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textTemperature = view.findViewById(R.id.tempTextView)
        textDescription = view.findViewById(R.id.descriptionTextView)
        recyclerView = view.findViewById(R.id.recyclerView)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}
