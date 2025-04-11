package github.yaa110.memento.adapter.template

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.widget.template.ModelViewHolder

abstract class ModelAdapter<T : DatabaseModel?, VH : ModelViewHolder<T>>(
    private val items: ArrayList<T>,
    private val selected: ArrayList<T>,
    private val listener: ClickListener<T>
) :
    RecyclerView.Adapter<VH>() {
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]

        // Populate view
        holder!!.populate(item)

        // Check if item is selected
        holder.setSelected(selected.contains(item))

        holder.holder.setOnClickListener { view: View? ->
            if (selected.isEmpty()) listener.onClick(item, items.indexOf(item))
            else toggleSelection(holder, item)
        }

        holder.holder.setOnLongClickListener { view: View? ->
            toggleSelection(holder, item)
            true
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun toggleSelection(holder: VH, item: T) {
        if (selected.contains(item)) {
            selected.remove(item)
            holder.setSelected(false)
            if (selected.isEmpty()) listener.onChangeSelection(false)
        } else {
            if (selected.isEmpty()) listener.onChangeSelection(true)
            selected.add(item)
            holder.setSelected(true)
        }
        listener.onCountSelection(selected.size)
    }

    interface ClickListener<M : DatabaseModel?> {
        fun onClick(item: M, position: Int)

        fun onChangeSelection(haveSelected: Boolean)

        fun onCountSelection(count: Int)
    }
}
