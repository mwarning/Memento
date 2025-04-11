package github.yaa110.memento.model

import android.content.ContentValues
import android.database.Cursor
import github.yaa110.memento.App
import github.yaa110.memento.R
import github.yaa110.memento.db.Controller
import github.yaa110.memento.db.OpenHelper
import java.util.Locale

class Category : DatabaseModel {
    var counter: Int = 0

    constructor()

    /**
     * Instantiates a new object of Category class using the data retrieved from database.
     *
     * @param c cursor object returned from a database query
     */
    constructor(c: Cursor) : super(c) {
        this.theme = c.getInt(c.getColumnIndex(OpenHelper.COLUMN_THEME))
        this.counter = c.getInt(c.getColumnIndex(OpenHelper.COLUMN_COUNTER))
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
                values.put(OpenHelper.COLUMN_COUNTER, counter)
                values.put(OpenHelper.COLUMN_ARCHIVED, isArchived)
            }

            values.put(OpenHelper.COLUMN_TITLE, title)
            values.put(OpenHelper.COLUMN_THEME, theme)

            return values
        }

    override fun equals(o: Any?): Boolean {
        return o != null && o is Category && id == (o.id)
    }

    companion object {
        const val THEME_RED: Int = 0
        const val THEME_PINK: Int = 1
        const val THEME_PURPLE: Int = 2
        const val THEME_BLUE: Int = 3
        const val THEME_CYAN: Int = 4
        const val THEME_TEAL: Int = 5
        const val THEME_GREEN: Int = 6
        const val THEME_AMBER: Int = 7
        const val THEME_ORANGE: Int = 8

        /**
         * @param theme the color id of category
         * @return the style of theme
         */
        fun getStyle(theme: Int): Int {
            when (theme) {
                THEME_RED -> return R.style.AppThemeRed
                THEME_PINK -> return R.style.AppThemePink
                THEME_AMBER -> return R.style.AppThemeAmber
                THEME_BLUE -> return R.style.AppThemeBlue
                THEME_CYAN -> return R.style.AppThemeCyan
                THEME_GREEN -> return R.style.AppThemeGreen
                THEME_ORANGE -> return R.style.AppThemeOrange
                THEME_PURPLE -> return R.style.AppThemePurple
                THEME_TEAL -> return R.style.AppThemeTeal
            }

            return R.style.AppTheme
        }

        /**
         * Reads a category by its id
         *
         * @param id primary key of category
         * @return the category object or null if it was not found
         */
        fun find(id: Long): Category {
            return Controller.instance!!.findNote(
                Category::class.java, id
            )!!
        }

        /**
         * Reads all categories
         *
         * @return a list of categories which is populated by database
         */
        fun all(): ArrayList<Category> {
            return Controller.instance!!.findNotes(
                Category::class.java,
                arrayOf(
                    OpenHelper.COLUMN_ID,
                    OpenHelper.COLUMN_TITLE,
                    OpenHelper.COLUMN_DATE,
                    OpenHelper.COLUMN_TYPE,
                    OpenHelper.COLUMN_ARCHIVED,
                    OpenHelper.COLUMN_THEME,
                    OpenHelper.COLUMN_COUNTER
                ),
                OpenHelper.COLUMN_TYPE + " = ? AND " + OpenHelper.COLUMN_ARCHIVED + " = ?",
                arrayOf(
                    String.format(Locale.US, "%d", TYPE_CATEGORY),
                    "0"
                ),
                App.sortCategoriesBy
            )
        }
    }
}
