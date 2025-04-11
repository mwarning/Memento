package github.yaa110.memento.widget.template

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import github.yaa110.memento.R
import github.yaa110.memento.model.DatabaseModel

abstract class ModelViewHolder<T : DatabaseModel?>(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var holder: View = itemView.findViewById(R.id.holder)
    var selected: View = itemView.findViewById(R.id.selected)

    fun setSelected(status: Boolean) {
        selected.visibility = if (status) View.VISIBLE else View.GONE
    }

    abstract fun populate(item: T)
}
