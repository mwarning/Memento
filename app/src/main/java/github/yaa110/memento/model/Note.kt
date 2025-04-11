package github.yaa110.memento.model

import android.content.ContentValues
import android.database.Cursor
import github.yaa110.memento.App
import github.yaa110.memento.db.Controller
import github.yaa110.memento.db.OpenHelper
import java.util.Locale

class Note : DatabaseModel {
    var categoryId: Long = 0
    var body: String? = null

    constructor()

    /**
     * Instantiates a new object of Note class using the data retrieved from database.
     *
     * @param c cursor object returned from a database query
     */
    constructor(c: Cursor) : super(c) {
        this.categoryId = c.getLong(c.getColumnIndex(OpenHelper.COLUMN_PARENT_ID))
        try {
            this.body = c.getString(c.getColumnIndex(OpenHelper.COLUMN_BODY))
        } catch (ignored: Exception) {
        }
    }

    override val contentValues: ContentValues
        /**
         * @return ContentValue object to be saved or updated
         */
        get() {
            val values = ContentValues()

            if (id == NEW_MODEL_ID) {
                values.put(OpenHelper.COLUMN_TYPE, type)
                values.put(OpenHelper.COLUMN_DATE, createdAt)
                values.put(OpenHelper.COLUMN_ARCHIVED, isArchived)
                values.put(OpenHelper.COLUMN_PARENT_ID, categoryId)
            }

            values.put(OpenHelper.COLUMN_TITLE, title)
            values.put(OpenHelper.COLUMN_BODY, body)

            return values
        }

    override fun equals(o: Any?): Boolean {
        return o != null && o is Note && id == (o.id)
    }

    companion object {
        /**
         * Reads a note by its id
         *
         * @param id primary key of note
         * @return the note object or null if it was not found
         */
        fun find(id: Long): Note? {
            return Controller.instance!!.findNote(
                Note::class.java, id
            )
        }

        /**
         * Reads all notes
         *
         * @param categoryId the id of parent category
         * @return a list of notes which is populated by database
         */
        fun all(categoryId: Long): ArrayList<Note> {
            return Controller.instance!!.findNotes(
                Note::class.java,
                arrayOf(
                    OpenHelper.COLUMN_ID,
                    OpenHelper.COLUMN_TITLE,
                    OpenHelper.COLUMN_DATE,
                    OpenHelper.COLUMN_TYPE,
                    OpenHelper.COLUMN_ARCHIVED,
                    OpenHelper.COLUMN_PARENT_ID,
                ),
                OpenHelper.COLUMN_TYPE + " != ? AND " + OpenHelper.COLUMN_PARENT_ID + " = ? AND " + OpenHelper.COLUMN_ARCHIVED + " = ?",
                arrayOf(
                    String.format(Locale.US, "%d", TYPE_CATEGORY),
                    String.format(Locale.US, "%d", categoryId),
                    "0"
                ),
                App.sortNotesBy
            )
        }
    }
}
