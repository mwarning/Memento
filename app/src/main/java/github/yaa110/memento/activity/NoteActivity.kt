package github.yaa110.memento.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import github.yaa110.memento.R
import github.yaa110.memento.db.OpenHelper
import github.yaa110.memento.fragment.DrawingNoteFragment
import github.yaa110.memento.fragment.SimpleNoteFragment
import github.yaa110.memento.fragment.template.NoteFragment
import github.yaa110.memento.model.Category
import github.yaa110.memento.model.DatabaseModel

class NoteActivity : AppCompatActivity(), NoteFragment.Callbacks {
    private var noteResult = 0
    private var position = 0

    private lateinit var fragment: NoteFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        val data = intent
        setTheme(
            Category.getStyle(
                data.getIntExtra(
                    OpenHelper.COLUMN_THEME,
                    Category.THEME_GREEN
                )
            )
        )
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        position = data.getIntExtra("position", 0)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        try {
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        } catch (ignored: Exception) {
        }

        toolbar.findViewById<View>(R.id.back_btn)
            .setOnClickListener { view: View? -> onBackPressed() }

        if (savedInstanceState == null) {
            fragment = if (data.getIntExtra(
                    OpenHelper.COLUMN_TYPE,
                    DatabaseModel.TYPE_NOTE_SIMPLE
                ) == DatabaseModel.TYPE_NOTE_SIMPLE
            ) {
                SimpleNoteFragment()
            } else {
                DrawingNoteFragment()
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.container, fragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        fragment.saveNote(object: NoteFragment.SaveListener {
            override fun onSave() {
                val data = Intent()
                data.putExtra("position", position)
                data.putExtra(OpenHelper.COLUMN_ID, fragment.note!!.id)

                when (noteResult) {
                    RESULT_NEW -> {
                        data.putExtra(OpenHelper.COLUMN_TYPE, fragment.note!!.type)
                        data.putExtra(OpenHelper.COLUMN_DATE, fragment.note!!.createdAt)
                        data.putExtra(OpenHelper.COLUMN_TITLE, fragment.note!!.title)
                    }

                    RESULT_EDIT -> data.putExtra(
                        OpenHelper.COLUMN_TITLE,
                        fragment.note!!.title
                    )
                }
                runOnUiThread {
                    setResult(noteResult, data)
                    finish()
                }
            }
        })
    }

    override fun setNoteResult(result: Int, closeActivity: Boolean) {
        noteResult = result
        if (closeActivity) {
            val data = Intent()
            data.putExtra("position", position)
            data.putExtra(OpenHelper.COLUMN_ID, fragment.note!!.id)
            setResult(result, data)
            finish()
        }
    }

    companion object {
        const val REQUEST_CODE: Int = 2
        const val RESULT_NEW: Int = 101
        const val RESULT_EDIT: Int = 102
        const val RESULT_DELETE: Int = 103
    }
}
