package fi.tuni.genericweatherapp

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import fi.tuni.genericweatherapp.databinding.FragmentWeatherBinding

class WeatherFragment : Fragment(R.layout.fragment_weather) {
    private val model: WeatherViewModel by activityViewModels()

    // Adapters used by recyclerView
    private val hourlyWeatherAdapter = HourlyWeatherAdapter()
    private val dailyWeatherAdapter = DailyWeatherAdapter()

    lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure recyclerViews' scroll direction and adapter to use
        val horizontalManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyRecyclerView.layoutManager = horizontalManager
        binding.hourlyRecyclerView.adapter = hourlyWeatherAdapter
        val verticalManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.dailyRecyclerView.layoutManager = verticalManager
        binding.dailyRecyclerView.adapter = dailyWeatherAdapter

        // Listen for successful forecast requests
        model.getWeather().observe(viewLifecycleOwner) { data ->
            // Change the background image
            binding.imageView.setImageDrawable(BitmapDrawable(resources, data.bitmap))
            // Add photo credits and a link to the image
            binding.photographerTextView.text = resources.getString(
                R.string.photographer_credit,
                data.photo.photographer
            )
            binding.linkTextView.text = data.photo.pageUrl
            // Set current weather temperature and description
            // TODO: Move formatting to ViewModel
            val symbol = model.getUnitsSymbol()
            binding.tempTextView.text =
                String.format("%.1f$symbol", data.weather.current.temp)
            binding.descriptionTextView.text = data.weather.current.description
            // Insert forecast for the next 12 hours into the recyclerView via adapter
            val hourly = data.weather.hourly
            hourlyWeatherAdapter.setItems(hourly.take(12))
            // Insert forecast for the next 7 days
            dailyWeatherAdapter.setItems(data.weather.daily.toList())
        }

        // TODO: Display loading icon while location is being fetched
        model.isLoading().observe(viewLifecycleOwner) { loading ->
            binding.tempTextView.text = if (loading) "Loading..." else "Loaded"
        }
    }
}
