package github.yaa110.memento.fragment

import android.view.View
import androidx.core.content.ContextCompat
import github.yaa110.memento.R
import github.yaa110.memento.fragment.template.NoteFragment
import github.yaa110.memento.model.DatabaseModel
import jp.wasabeef.richeditor.RichEditor

class SimpleNoteFragment : NoteFragment() {
    private lateinit var body: RichEditor

    override val layout: Int
        get() = R.layout.fragment_simple_note

    override fun saveNote(listener: SaveListener) {
        super.saveNote(listener)
        note!!.body = body.html

        object : Thread() {
            override fun run() {
                val id = note!!.save()
                if (note!!.id == DatabaseModel.NEW_MODEL_ID) {
                    note!!.id = id
                }
                listener.onSave()
                interrupt()
            }
        }.start()
    }

    override fun init(view: View) {
        body = view.findViewById(R.id.editor)
        body.setPlaceholder("Note")
        body.setEditorBackgroundColor(ContextCompat.getColor(requireContext(), R.color.bg))

        view.findViewById<View>(R.id.action_bold).setOnClickListener { v: View? -> body.setBold() }

        view.findViewById<View>(R.id.action_italic)
            .setOnClickListener { v: View? -> body.setItalic() }

        view.findViewById<View>(R.id.action_underline)
            .setOnClickListener { v: View? -> body.setUnderline() }

        body.setHtml(note!!.body)
    }
}
