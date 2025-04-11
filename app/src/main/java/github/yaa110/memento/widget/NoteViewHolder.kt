package github.yaa110.memento.widget

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import github.yaa110.memento.R
import github.yaa110.memento.inner.Formatter
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.model.Note
import github.yaa110.memento.widget.template.ModelViewHolder

class NoteViewHolder(itemView: View) : ModelViewHolder<Note>(itemView) {
    var badge: ImageView =
        itemView.findViewById(R.id.badge_icon)
    var title: TextView = itemView.findViewById(R.id.title_txt)
    var date: TextView = itemView.findViewById(R.id.date_txt)

    override fun populate(item: Note) {
        if (item.type == DatabaseModel.Companion.TYPE_NOTE_DRAWING) {
            badge.setImageResource(R.drawable.fab_drawing)
        } else {
            badge.setImageResource(R.drawable.fab_type)
        }
        title.text = item.title
        date.text = Formatter.formatShortDate(item.createdAt)
    }
}
