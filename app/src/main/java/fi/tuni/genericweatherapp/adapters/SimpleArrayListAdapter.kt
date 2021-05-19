package fi.tuni.genericweatherapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Generic RecyclerView adapter for ArrayList.
 *
 * @param itemLayout The layout resource to use as the list items' views.
 * @param T The data object to store in ArrayList.
 * @param E A class implementing [RecyclerView.ViewHolder], used for binding item views with data.
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

    /**
     * Method used by [onCreateViewHolder] to create new Holder objects.
     *
     * @param view The View to pass to [RecyclerView.ViewHolder]'s constructor
     */
    abstract fun holderFactory(view: View): E

    /**
     * Called when the contents of the [holder] should be changed to reflect the contents of [item].
     *
     * Simplified version of [onBindViewHolder], directly referencing the data object instead of its
     * index in the list.
     *
     * @param holder The view holder whose contents should be updated to match the given data.
     * @param item The data to bind to the view holder.
     */
    abstract fun onBind(holder: E, item: T)

    override fun getItemCount() = data.size

    fun isEmpty() = data.isEmpty()

    /**
     * Clear the data set.
     */
    fun clearItems() {
        data.clear()
        notifyDataSetChanged()
    }

    /**
     * Insert a single [item] to [index].
     */
    fun set(index: Int, item: T) {
        data[index] = item
        notifyDataSetChanged()
    }

    /**
     * Add a single [item] to the list
     */
    fun add(item: T) {
        data.add(item)
        notifyDataSetChanged()
    }

    /**
     * Replace the data set with [items].
     */
    fun setItems(items: List<T>) {
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }
}
