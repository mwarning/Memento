package github.yaa110.memento.widget

import android.view.View
import android.widget.TextView
import github.yaa110.memento.R
import github.yaa110.memento.inner.Formatter
import github.yaa110.memento.model.Category
import github.yaa110.memento.widget.template.ModelViewHolder
import java.util.Locale

class CategoryViewHolder(itemView: View) : ModelViewHolder<Category>(itemView) {
    var badge: TextView = itemView.findViewById(R.id.badge_txt)
    var title: TextView = itemView.findViewById(R.id.title_txt)
    var date: TextView = itemView.findViewById(R.id.date_txt)
    var counter: TextView = itemView.findViewById(R.id.counter_txt)

    override fun populate(item: Category) {
        badge.text = item.title!!.substring(0, 1).uppercase()
        badge.setBackgroundResource(item.themeBackground)
        title.text = item.title
        if (item.counter == 0) counter.text = ""
        else if (item.counter == 1) counter.setText(R.string.one_note)
        else counter.text = String.format(Locale.US, "%d notes", item.counter)
        date.text = Formatter.formatShortDate(item.createdAt)
    }
}
