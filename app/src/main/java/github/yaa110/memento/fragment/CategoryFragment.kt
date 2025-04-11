package github.yaa110.memento.fragment

import android.content.Intent
import android.view.View
import com.google.android.material.snackbar.Snackbar
import github.yaa110.memento.R
import github.yaa110.memento.activity.NoteActivity
import github.yaa110.memento.adapter.NoteAdapter
import github.yaa110.memento.adapter.template.ModelAdapter
import github.yaa110.memento.db.Controller
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.fragment.template.RecyclerFragment
import github.yaa110.memento.inner.Animator
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.model.Note
import java.util.Locale

class CategoryFragment : RecyclerFragment<Note, NoteAdapter>() {
    lateinit var protector: View
    lateinit var fab_type: View
    lateinit var fab_drawing: View
    var isFabOpen: Boolean = false

    override val listener: ModelAdapter.ClickListener<*> =
        object : ModelAdapter.ClickListener<DatabaseModel> {
            override fun onClick(item: DatabaseModel, position: Int) {
                startNoteActivity(item!!.type, item.id, position)
            }

            override fun onChangeSelection(haveSelected: Boolean) {
                toggleSelection(haveSelected)
            }

            override fun onCountSelection(count: Int) {
                onChangeCounter(count)
            }
        }

    override fun init(view: View) {
        protector = view.findViewById(R.id.protector)
        fab_type = view.findViewById(R.id.fab_type)
        fab_drawing = view.findViewById(R.id.fab_drawing)

        protector.setOnClickListener { view1: View? -> toggleFab(true) }

        fab_type.setOnClickListener { view2: View? ->
            startNoteActivity(
                DatabaseModel.TYPE_NOTE_SIMPLE,
                DatabaseModel.NEW_MODEL_ID,
                0
            )
        }

        fab_drawing.setOnClickListener { view3: View? ->
            startNoteActivity(
                DatabaseModel.TYPE_NOTE_DRAWING,
                DatabaseModel.NEW_MODEL_ID,
                0
            )
        }
    }

    private fun startNoteActivity(type: Int, noteId: Long, position: Int) {
        toggleFab(true)

        object : Thread() {
            override fun run() {
                try {
                    sleep(150)
                } catch (ignored: InterruptedException) {
                }

                requireActivity().runOnUiThread {
                    val intent = Intent(context, NoteActivity::class.java)
                    intent.putExtra(OpenHelper.COLUMN_TYPE, type)
                    intent.putExtra("position", position)
                    intent.putExtra(OpenHelper.COLUMN_ID, noteId)
                    intent.putExtra(OpenHelper.COLUMN_PARENT_ID, categoryId)
                    intent.putExtra(OpenHelper.COLUMN_THEME, categoryTheme)
                    startActivityForResult(intent, NoteActivity.REQUEST_CODE)
                }

                interrupt()
            }
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null && requestCode == NoteActivity.REQUEST_CODE) {
            val position = data.getIntExtra("position", 0)

            when (resultCode) {
                NoteActivity.RESULT_NEW -> {
                    val note = Note()
                    note.title = data.getStringExtra(OpenHelper.COLUMN_TITLE)
                    note.type = data.getIntExtra(
                        OpenHelper.COLUMN_TYPE,
                        DatabaseModel.TYPE_NOTE_SIMPLE
                    )
                    note.createdAt = data.getLongExtra(
                        OpenHelper.COLUMN_DATE,
                        System.currentTimeMillis()
                    )
                    note.id = data.getLongExtra(
                        OpenHelper.COLUMN_ID,
                        DatabaseModel.NEW_MODEL_ID
                    )
                    addItem(note, position)
                }

                NoteActivity.RESULT_EDIT -> {
                    val item = items!![position]!!
                    item.title = data.getStringExtra(OpenHelper.COLUMN_TITLE)
                    refreshItem(position)
                }

                NoteActivity.RESULT_DELETE -> object : Thread() {
                    override fun run() {
                        Controller.instance!!.deleteNotes(
                            arrayOf(
                                String.format(
                                    Locale.US,
                                    "%d",
                                    data.getLongExtra(
                                        OpenHelper.COLUMN_ID,
                                        DatabaseModel.NEW_MODEL_ID
                                    )
                                )
                            ),
                            categoryId
                        )

                        requireActivity().runOnUiThread {
                            val deletedItem = deleteItem(position)
                            Snackbar.make(
                                (if (fab != null) fab else selectionToolbar),
                                "1 note was deleted",
                                7000
                            )
                                .setAction(R.string.undo) { view: View? ->
                                    object : Thread() {
                                        override fun run() {
                                            Controller.instance!!.undoDeletion()
                                            Controller.instance!!.addCategoryCounter(
                                                deletedItem!!.categoryId, 1
                                            )

                                            requireActivity().runOnUiThread {
                                                addItem(
                                                    deletedItem,
                                                    position
                                                )
                                            }

                                            interrupt()
                                        }
                                    }.start()
                                }
                                .show()
                        }

                        interrupt()
                    }
                }.start()
            }
        }
    }

    override fun onClickFab() {
        toggleFab(false)
    }

    fun toggleFab(forceClose: Boolean) {
        if (isFabOpen) {
            isFabOpen = false

            Animator.create(context)
                .on<View?>(protector)
                .setEndVisibility(View.GONE)
                .animate(R.anim.fade_out)

            Animator.create(context)
                .on(fab)
                .animate(R.anim.fab_rotate_back)

            Animator.create(context)
                .on<View?>(fab_type)
                .setEndVisibility(View.GONE)
                .animate(R.anim.fab_out)

            Animator.create(context)
                .on<View?>(fab_drawing)
                .setDelay(50)
                .setEndVisibility(View.GONE)
                .animate(R.anim.fab_out)
        } else if (!forceClose) {
            isFabOpen = true

            Animator.create(context)
                .on(protector)
                .setStartVisibility(View.VISIBLE)
                .animate(R.anim.fade_in)

            Animator.create(context)
                .on(fab)
                .animate(R.anim.fab_rotate)

            Animator.create(context)
                .on(fab_type)
                .setDelay(80)
                .setStartVisibility(View.VISIBLE)
                .animate(R.anim.fab_in)

            Animator.create(context)
                .on(fab_drawing)
                .setStartVisibility(View.VISIBLE)
                .animate(R.anim.fab_in)
        }
    }

    override val layout: Int
        get() = R.layout.fragment_category

    override val itemName: String
        get() = "note"

    override val adapterClass: Class<NoteAdapter>
        get() = NoteAdapter::class.java
}
