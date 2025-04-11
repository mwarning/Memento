package github.yaa110.memento.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import github.yaa110.memento.R
import github.yaa110.memento.adapter.template.ModelAdapter
import github.yaa110.memento.model.Note
import github.yaa110.memento.widget.NoteViewHolder

class NoteAdapter(
    items: ArrayList<Note>,
    selected: ArrayList<Note>,
    listener: ClickListener<Note>
) : ModelAdapter<Note, NoteViewHolder>(items, selected, listener) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        )
    }
}