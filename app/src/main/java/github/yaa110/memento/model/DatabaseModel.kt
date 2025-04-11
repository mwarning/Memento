package github.yaa110.memento.model

import android.content.ContentValues
import android.database.Cursor
import github.yaa110.memento.R
import github.yaa110.memento.db.Controller
import github.yaa110.memento.db.OpenHelper

abstract class DatabaseModel {
    var id: Long = NEW_MODEL_ID
    var type: Int = 0
    var title: String? = null
    var createdAt: Long = 0
    var isArchived: Boolean = false
    var theme: Int = 0

    var position: Int = 0

    constructor()

    /**
     * Instantiates a new object of DatabaseModel class using the data retrieved from database.
     *
     * @param c cursor object returned from a database query
     */
    constructor(c: Cursor) {
        this.id = c.getLong(c.getColumnIndex(OpenHelper.COLUMN_ID))
        this.type = c.getInt(c.getColumnIndex(OpenHelper.COLUMN_TYPE))
        this.title = c.getString(c.getColumnIndex(OpenHelper.COLUMN_TITLE))
        try {
            this.createdAt =
                c.getString(c.getColumnIndex(OpenHelper.COLUMN_DATE)).toLong()
        } catch (nfe: NumberFormatException) {
            this.createdAt = System.currentTimeMillis()
        }
        this.isArchived = c.getInt(c.getColumnIndex(OpenHelper.COLUMN_ARCHIVED)) == 1
    }

    /**
     * Inserts or updates a note or category
     *
     * @return true if the note is saved.
     */
    fun save(): Long {
        return Controller.instance!!.saveNote(
            this,
            contentValues
        )
    }

    /**
     * Toggle archived state and
     *
     * @return true if the action is completed.
     */
    fun toggle(): Boolean {
        val values = ContentValues()
        values.put(OpenHelper.COLUMN_ARCHIVED, !isArchived)

        if (Controller.instance!!.saveNote(this, values) != NEW_MODEL_ID) {
            isArchived = !isArchived
            return true
        }

        return false
    }

    val themeBackground: Int
        /**
         * @return color of the theme
         */
        get() {
            when (theme) {
                Category.THEME_RED -> return R.drawable.circle_red
                Category.THEME_PINK -> return R.drawable.circle_pink
                Category.THEME_AMBER -> return R.drawable.circle_amber
                Category.THEME_BLUE -> return R.drawable.circle_blue
                Category.THEME_CYAN -> return R.drawable.circle_cyan
                Category.THEME_GREEN -> return R.drawable.circle_green
                Category.THEME_ORANGE -> return R.drawable.circle_orange
                Category.THEME_PURPLE -> return R.drawable.circle_purple
                Category.THEME_TEAL -> return R.drawable.circle_teal
            }

            return R.drawable.circle_main
        }

    /**
     * @return ContentValue object to be saved or updated
     */
    abstract val contentValues: ContentValues

    override fun hashCode(): Int {
        return id.toInt()
    }

    override fun equals(o: Any?): Boolean {
        return o != null && o is DatabaseModel && id == (o.id)
    }

    companion object {
        const val TYPE_CATEGORY: Int = 0
        const val TYPE_NOTE_SIMPLE: Int = 1
        const val TYPE_NOTE_DRAWING: Int = 2

        const val NEW_MODEL_ID: Long = -1
    }
}
