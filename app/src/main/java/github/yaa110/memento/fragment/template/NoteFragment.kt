package github.yaa110.memento.fragment.template

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import github.yaa110.memento.R
import github.yaa110.memento.activity.NoteActivity
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.model.Note

abstract class NoteFragment : Fragment() {
    var note: Note? = null
    var activity: Callbacks? = null
    lateinit var title: EditText
    private lateinit var deleteBtn: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteBtn = requireActivity().findViewById(R.id.delete_btn)
        title = view.findViewById(R.id.title_txt)

        val data = requireActivity().intent
        val noteId =
            data.getLongExtra(OpenHelper.COLUMN_ID, DatabaseModel.NEW_MODEL_ID)
        val categoryId = data.getLongExtra(
            OpenHelper.COLUMN_PARENT_ID,
            DatabaseModel.NEW_MODEL_ID
        )

        if (noteId != DatabaseModel.NEW_MODEL_ID) {
            note = Note.find(noteId)
        }

        if (note == null) {
            note = Note()
            activity!!.setNoteResult(NoteActivity.RESULT_NEW, false)
            deleteBtn.visibility = View.GONE
            note!!.categoryId = categoryId
            note!!.title = ""
            note!!.body = ""
            note!!.isArchived = false
            note!!.type = data.getIntExtra(
                OpenHelper.COLUMN_TYPE,
                DatabaseModel.TYPE_NOTE_SIMPLE
            )
        } else {
            activity!!.setNoteResult(NoteActivity.RESULT_EDIT, false)
            deleteBtn.visibility = View.VISIBLE
            deleteBtn.setOnClickListener { view1: View? ->
                activity!!.setNoteResult(
                    NoteActivity.RESULT_DELETE,
                    true
                )
            }
        }

        title.setText(note!!.title)

        init(view)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as Callbacks
    }

    open fun saveNote(listener: SaveListener) {
        var inputTitle = title!!.text.toString()
        if (inputTitle.isEmpty()) inputTitle = "Untitled"
        note!!.title = inputTitle
        if (note!!.id == DatabaseModel.NEW_MODEL_ID) {
            note!!.createdAt = System.currentTimeMillis()
        }
    }

    abstract val layout: Int

    abstract fun init(view: View)

    interface SaveListener {
        fun onSave()
    }

    interface Callbacks {
        fun setNoteResult(result: Int, closeActivity: Boolean)
    }
}
