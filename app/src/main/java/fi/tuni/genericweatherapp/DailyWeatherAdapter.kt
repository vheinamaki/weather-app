package fi.tuni.genericweatherapp

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * RecyclerView adapter for daily weather forecast items
 */
class DailyWeatherAdapter :
    SimpleArrayListAdapter<OpenWeatherMap.Daily, DailyWeatherAdapter.Holder>(R.layout.item_weather_daily) {
    var symbol = ""

    // Format as day of the week
    private val formatter = SimpleDateFormat("EEEE", Locale.ENGLISH)

    // Used to check if two Date objects have the same date
    private val sameDateFmt = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.findViewById(R.id.dayTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
    }


    override fun onBind(holder: Holder, item: OpenWeatherMap.Daily) {
        val today = sameDateFmt.format(item.time) == sameDateFmt.format(Date())
        holder.dayTextView.text =
            if (today) MainApplication.res.getString(R.string.today) else formatter.format(item.time)
        holder.descriptionTextView.text =
            "${item.temp.max.roundToInt()}$symbol / ${item.temp.min.roundToInt()}$symbol"
    }

    override fun holderFactory(view: View) = Holder(view)
}
