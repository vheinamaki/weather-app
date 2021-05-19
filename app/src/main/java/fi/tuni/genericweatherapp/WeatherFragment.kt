package fi.tuni.genericweatherapp

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import fi.tuni.genericweatherapp.adapters.HourlyWeatherAdapter
import fi.tuni.genericweatherapp.data.OpenWeatherMap
import fi.tuni.genericweatherapp.databinding.FragmentWeatherBinding
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherFragment : Fragment(R.layout.fragment_weather) {
    private val model: WeatherViewModel by activityViewModels()

    /**
     * Format as date, without time.
     */
    private val dateFmt = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH)

    /**
     * Format as hours and minutes.
     */
    private val timeFmt = SimpleDateFormat.getTimeInstance(DateFormat.SHORT, Locale.ENGLISH)

    /**
     * Format as day of the week.
     */
    private val weekdayFmt = SimpleDateFormat("EEEE", Locale.ENGLISH)

    /**
     * Used to check if two Date objects have the same date.
     */
    private val sameDateFmt = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)

    /**
     * Adapter used by hourly forecast RecyclerView.
     */
    private val hourlyWeatherAdapter = HourlyWeatherAdapter()

    /**
     * ViewBinding for the fragment. Replaces findViewById calls with properties available on
     * the auto-generated binding class.
     */
    private lateinit var binding: FragmentWeatherBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Setup view binding
        binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configure recyclerView's scroll direction and adapter to use
        val horizontalManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.hourlyRecyclerView.layoutManager = horizontalManager
        binding.hourlyRecyclerView.adapter = hourlyWeatherAdapter

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
            binding.weatherImage.setImageResource(weatherIconToDrawableResource(data.weather.current.icon))
            // Set current weather temperature and description
            // TODO: Move formatting to ViewModel
            val symbol = model.getUnitsSymbol()
            binding.tempTextView.text = "${data.weather.current.temp.roundToInt()}$symbol"
            // Capitalize the description
            binding.description.text = data.weather.current.description.replaceFirstChar {
                it.uppercase()
            }
            binding.currentDayText.text =
                "${dateFmt.format(data.weather.current.time)} ${timeFmt.format(data.weather.current.time)}"
            binding.feelsLike.text = resources.getString(
                R.string.feels_like,
                "${data.weather.current.feelsLike.roundToInt()}$symbol"
            )
            binding.precipitation.text =
                resources.getString(R.string.humidity, "${data.weather.current.humidity}%")
            // Insert forecast for the next 12 hours into the recyclerView via adapter
            val hourly = data.weather.hourly
            hourlyWeatherAdapter.symbol = symbol
            hourlyWeatherAdapter.setItems(hourly.take(12))
            // Insert forecast for the next 7 days
            fillDailyWeather(data.weather.daily, symbol)
        }
    }

    /**
     * Helper function to fill the daily weather list with data.
     *
     * @param items The items to insert into the list.
     * @param tempSymbol The temperature symbol the items should use.
     */
    private fun fillDailyWeather(items: Array<OpenWeatherMap.Daily>, tempSymbol: String) {
        val now = Date()
        binding.dailyWeatherList.removeAllViews()
        items.forEach {
            val item = layoutInflater.inflate(R.layout.item_weather_daily, null)
            val day: TextView = item.findViewById(R.id.dayTextView)
            val description: TextView = item.findViewById(R.id.descriptionTextView)
            val humidity: TextView = item.findViewById(R.id.humidityTextView)
            val image: ImageView = item.findViewById(R.id.imageView)

            val isToday = sameDateFmt.format(it.time) == sameDateFmt.format(now)
            day.text =
                if (isToday) MainApplication.res.getString(R.string.today) else weekdayFmt.format(it.time)
            description.text =
                "${it.temp.max.roundToInt()}$tempSymbol / ${it.temp.min.roundToInt()}$tempSymbol"
            humidity.text = "${it.humidity}%"
            image.setImageResource(weatherIconToDrawableResource(it.icon))
            binding.dailyWeatherList.addView(item)
        }
    }
}
