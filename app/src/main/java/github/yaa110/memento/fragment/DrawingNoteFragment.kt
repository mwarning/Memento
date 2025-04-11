package github.yaa110.memento.fragment

import android.util.Base64
import android.view.View
import com.android.graphics.CanvasView
import github.yaa110.memento.R
import github.yaa110.memento.fragment.template.NoteFragment
import github.yaa110.memento.model.DatabaseModel

class DrawingNoteFragment : NoteFragment() {
    private lateinit var canvas: CanvasView

    override val layout: Int
        get() = R.layout.fragment_drawing_note

    override fun saveNote(listener: SaveListener) {
        super.saveNote(listener)

        object : Thread() {
            override fun run() {
                note!!.body = Base64.encodeToString(canvas.bitmapAsByteArray, Base64.NO_WRAP)

                val id = note!!.save()
                if (note!!.id == DatabaseModel.Companion.NEW_MODEL_ID) {
                    note!!.id = id
                }

                listener.onSave()
                interrupt()
            }
        }.start()
    }

    override fun init(view: View) {
        canvas = view.findViewById(R.id.canvas)

        view.findViewById<View>(R.id.pen_tool).setOnClickListener { view1: View? ->
            canvas.mode = CanvasView.Mode.DRAW
            canvas.paintStrokeWidthValue = 3f
        }

        view.findViewById<View>(R.id.eraser_tool).setOnClickListener { view2: View? ->
            canvas.mode = CanvasView.Mode.ERASER
            canvas.paintStrokeWidthValue = 40f
        }

        if (note!!.body!!.isNotEmpty()) {
            canvas.drawBitmap(Base64.decode(note!!.body, Base64.NO_WRAP))
        }
    }
}
