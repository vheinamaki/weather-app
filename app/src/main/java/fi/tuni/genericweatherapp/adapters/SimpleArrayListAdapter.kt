package fi.tuni.genericweatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Generic RecyclerView adapter for ArrayList
 */
abstract class SimpleArrayListAdapter<T, E : RecyclerView.ViewHolder>(
    private val itemLayout: Int
) :
    RecyclerView.Adapter<E>() {
    private val data = ArrayList<T>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): E {
        val view = LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
        return holderFactory(view)
    }

    override fun onBindViewHolder(holder: E, position: Int) {
        onBind(holder, data[position])
    }

    // Needed to create an object of generic type in onCreateViewHolder
    abstract fun holderFactory(view: View): E

    abstract fun onBind(holder: E, item: T)

    override fun getItemCount() = data.size

    // Clear the data set
    fun clearItems() {
        data.clear()
        notifyDataSetChanged()
    }

    fun set(index: Int, item: T) {
        if (data.isEmpty()) {
            data.add(item)
        } else {
            data[index] = item
        }
        notifyDataSetChanged()
    }

    // Replace the data set
    fun setItems(items: List<T>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}
