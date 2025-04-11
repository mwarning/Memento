package github.yaa110.memento.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import github.yaa110.memento.model.DatabaseModel
import github.yaa110.memento.model.Note
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

class Controller private constructor(context: Context) {
    private val helper: SQLiteOpenHelper = OpenHelper(context)
    private val sorts = arrayOf<String>(
        OpenHelper.COLUMN_TITLE + " ASC",
        OpenHelper.COLUMN_TITLE + " DESC",
        OpenHelper.COLUMN_ID + " ASC",
        OpenHelper.COLUMN_ID + " DESC",
    )

    /**
     * Reads data from json array
     *
     * @param json an array of json objects
     * @throws Exception
     */
    @Throws(Exception::class)
    fun readBackup(json: JSONArray) {
        val db = helper.readableDatabase

        try {
            val length = json.length()
            for (i in 0..<length) {
                val item = json.getJSONObject(i)

                val values = ContentValues()
                values.put(
                    OpenHelper.COLUMN_ID,
                    item.getLong(OpenHelper.COLUMN_ID)
                )
                values.put(
                    OpenHelper.COLUMN_TITLE,
                    item.getString(OpenHelper.COLUMN_TITLE)
                )
                values.put(
                    OpenHelper.COLUMN_BODY,
                    item.getString(OpenHelper.COLUMN_BODY)
                )
                values.put(
                    OpenHelper.COLUMN_TYPE,
                    item.getInt(OpenHelper.COLUMN_TYPE)
                )
                values.put(
                    OpenHelper.COLUMN_DATE,
                    item.getString(OpenHelper.COLUMN_DATE)
                )
                values.put(
                    OpenHelper.COLUMN_ARCHIVED,
                    item.getInt(OpenHelper.COLUMN_ARCHIVED)
                )
                values.put(
                    OpenHelper.COLUMN_THEME,
                    item.getInt(OpenHelper.COLUMN_THEME)
                )
                values.put(
                    OpenHelper.COLUMN_COUNTER,
                    item.getInt(OpenHelper.COLUMN_COUNTER)
                )
                values.put(
                    OpenHelper.COLUMN_PARENT_ID,
                    item.getLong(OpenHelper.COLUMN_PARENT_ID)
                )
                values.put(
                    OpenHelper.COLUMN_EXTRA,
                    item.getString(OpenHelper.COLUMN_EXTRA)
                )

                db.replace(
                    OpenHelper.TABLE_NOTES,
                    null,
                    values
                )
            }
        } finally {
            db.close()
        }
    }

    /**
     * Writes data to file
     *
     * @param fos an object of FileOutputStream
     * @throws Exception
     */
    @Throws(Exception::class)
    fun writeBackup(fos: OutputStream) {
        val db = helper.readableDatabase

        try {
            val c = db.query(
                OpenHelper.TABLE_NOTES,
                null, null, null, null, null, null
            )

            if (c != null) {
                var needComma = false
                while (c.moveToNext()) {
                    if (needComma) {
                        fos.write(",".toByteArray(StandardCharsets.UTF_8))
                    } else {
                        needComma = true
                    }

                    val item = JSONObject()
                    item.put(
                        OpenHelper.COLUMN_ID,
                        c.getLong(c.getColumnIndex(OpenHelper.COLUMN_ID))
                    )
                    item.put(
                        OpenHelper.COLUMN_TITLE,
                        c.getString(c.getColumnIndex(OpenHelper.COLUMN_TITLE))
                    )
                    item.put(
                        OpenHelper.COLUMN_BODY,
                        c.getString(c.getColumnIndex(OpenHelper.COLUMN_BODY))
                    )
                    item.put(
                        OpenHelper.COLUMN_TYPE,
                        c.getInt(c.getColumnIndex(OpenHelper.COLUMN_TYPE))
                    )
                    item.put(
                        OpenHelper.COLUMN_DATE,
                        c.getString(c.getColumnIndex(OpenHelper.COLUMN_DATE))
                    )
                    item.put(
                        OpenHelper.COLUMN_ARCHIVED,
                        c.getInt(c.getColumnIndex(OpenHelper.COLUMN_ARCHIVED))
                    )
                    item.put(
                        OpenHelper.COLUMN_THEME,
                        c.getInt(c.getColumnIndex(OpenHelper.COLUMN_THEME))
                    )
                    item.put(
                        OpenHelper.COLUMN_COUNTER,
                        c.getInt(c.getColumnIndex(OpenHelper.COLUMN_COUNTER))
                    )
                    item.put(
                        OpenHelper.COLUMN_PARENT_ID,
                        c.getLong(c.getColumnIndex(OpenHelper.COLUMN_PARENT_ID))
                    )
                    item.put(
                        OpenHelper.COLUMN_EXTRA,
                        c.getString(c.getColumnIndex(OpenHelper.COLUMN_EXTRA))
                    )

                    fos.write(item.toString().toByteArray(StandardCharsets.UTF_8))
                }

                c.close()
            }
        } finally {
            db.close()
        }
    }

    /**
     * Reads all notes or categories from database
     *
     * @param cls         the class of the model type
     * @param columns     the columns must be returned from the query
     * @param where       the where clause of the query.
     * @param whereParams the parameters of where clause.
     * @param sortId      the sort id of categories or notes
     * @param <T>         a type which extends DatabaseModel
     * @return a list of notes or categories
    </T> */
    fun <T : DatabaseModel?> findNotes(
        cls: Class<T>,
        columns: Array<String>?,
        where: String?,
        whereParams: Array<String>?,
        sortId: Int
    ): ArrayList<T> {
        val items = ArrayList<T>()

        val db = helper.readableDatabase

        try {
            val c = db.query(
                OpenHelper.TABLE_NOTES,
                columns,
                where,
                whereParams,
                null, null,
                sorts[sortId]
            )

            if (c != null) {
                while (c.moveToNext()) {
                    try {
                        items.add(cls.getDeclaredConstructor(Cursor::class.java).newInstance(c))
                    } catch (ignored: Exception) {
                    }
                }

                c.close()
            }

            return items
        } finally {
            db.close()
        }
    }

    /**
     * Reads a note or category from the database
     *
     * @param cls the class of the model type
     * @param id  primary key of note or category
     * @param <T> a type which extends DatabaseModel
     * @return a new object of T type
    </T> */
    fun <T : DatabaseModel?> findNote(cls: Class<T>, id: Long): T? {
        val db = helper.readableDatabase

        try {
            val c = db.query(
                OpenHelper.TABLE_NOTES,
                null,
                OpenHelper.COLUMN_ID + " = ?",
                arrayOf<String>(
                    String.format(Locale.US, "%d", id)
                ),
                null, null, null
            )

            if (c == null) return null

            if (c.moveToFirst()) {
                return try {
                    cls.getDeclaredConstructor(Cursor::class.java).newInstance(c)
                } catch (e: Exception) {
                    null
                }
            }

            return null
        } finally {
            db.close()
        }
    }

    /**
     * Change the amount of category counter
     *
     * @param categoryId the id of category
     * @param amount     to be added (negative or positive)
     */
    fun addCategoryCounter(categoryId: Long, amount: Int) {
        val db = helper.writableDatabase

        try {
            val c = db.rawQuery(
                "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " + ? WHERE " + OpenHelper.COLUMN_ID + " = ?",
                arrayOf<String>(
                    String.format(Locale.US, "%d", amount),
                    String.format(Locale.US, "%d", categoryId)
                )
            )

            if (c != null) {
                c.moveToFirst()
                c.close()
            }
        } finally {
            db.close()
        }
    }

    /**
     * Restores last deleted notes
     */
    fun undoDeletion() {
        val db = helper.writableDatabase

        try {
            val c = db.query(
                OpenHelper.TABLE_UNDO,
                null, null, null, null, null, null
            )

            if (c != null) {
                while (c.moveToNext()) {
                    val query = c.getString(c.getColumnIndex(OpenHelper.COLUMN_SQL))
                    if (query != null) {
                        val nc = db.rawQuery(
                            query,
                            null
                        )

                        if (nc != null) {
                            nc.moveToFirst()
                            nc.close()
                        }
                    }
                }

                c.close()
            }

            clearUndoTable(db)
        } finally {
            db.close()
        }
    }

    /**
     * Clears the undo table
     *
     * @param db an object of writable SQLiteDatabase
     */
    fun clearUndoTable(db: SQLiteDatabase) {
        val uc = db.rawQuery("DELETE FROM " + OpenHelper.TABLE_UNDO, null)
        if (uc != null) {
            uc.moveToFirst()
            uc.close()
        }
    }

    /**
     * Deletes a note or category (and its children) from the database
     *
     * @param ids        a list of the notes' IDs
     * @param categoryId the id of parent category
     */
    fun deleteNotes(ids: Array<String?>, categoryId: Long) {
        val db = helper.writableDatabase

        try {
            clearUndoTable(db)

            val where = StringBuilder()
            val childWhere = StringBuilder()

            var needOR = false
            for (i in ids.indices) {
                if (needOR) {
                    where.append(" OR ")
                    childWhere.append(" OR ")
                } else {
                    needOR = true
                }
                where.append(OpenHelper.COLUMN_ID).append(" = ?")
                childWhere.append(OpenHelper.COLUMN_PARENT_ID).append(" = ?")
            }

            val count = db.delete(
                OpenHelper.TABLE_NOTES,
                where.toString(),
                ids
            )

            if (categoryId == DatabaseModel.NEW_MODEL_ID) {
                db.delete(
                    OpenHelper.TABLE_NOTES,
                    childWhere.toString(),
                    ids
                )
            } else {
                val c = db.rawQuery(
                    "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " - ? WHERE " + OpenHelper.COLUMN_ID + " = ?",
                    arrayOf<String>(
                        String.format(Locale.US, "%d", count),
                        String.format(Locale.US, "%d", categoryId)
                    )
                )

                if (c != null) {
                    c.moveToFirst()
                    c.close()
                }
            }
        } finally {
            db.close()
        }
    }

    /**
     * Inserts or updates a note or category in the database and increments the counter
     * of category if the deleted object is an instance of Note class
     *
     * @param note   the object of type T
     * @param values ContentValuse of the object to be inserted or updated
     * @param <T>    a type which extends DatabaseModel
     * @return the id of saved note
    </T> */
    fun <T : DatabaseModel?> saveNote(note: T, values: ContentValues?): Long {
        val db = helper.writableDatabase

        try {
            if (note!!.id > DatabaseModel.NEW_MODEL_ID) {
                // Update note
                db.update(
                    OpenHelper.TABLE_NOTES,
                    note.contentValues,
                    OpenHelper.COLUMN_ID + " = ?",
                    arrayOf<String>(
                        String.format(Locale.US, "%d", note.id)
                    )
                )
                return note.id
            } else {
                // Create a new note
                note.id = db.insert(
                    OpenHelper.TABLE_NOTES,
                    null,
                    values
                )

                if (note is Note) {
                    // Increment the counter of category
                    val c = db.rawQuery(
                        "UPDATE " + OpenHelper.TABLE_NOTES + " SET " + OpenHelper.COLUMN_COUNTER + " = " + OpenHelper.COLUMN_COUNTER + " + 1 WHERE " + OpenHelper.COLUMN_ID + " = ?",
                        arrayOf<String>(
                            String.format(Locale.US, "%d", (note as Note).categoryId)
                        )
                    )

                    if (c != null) {
                        c.moveToFirst()
                        c.close()
                    }
                }

                return note.id
            }
        } catch (e: Exception) {
            return DatabaseModel.NEW_MODEL_ID
        } finally {
            db.close()
        }
    }

    companion object {
        const val SORT_TITLE_ASC: Int = 0
        const val SORT_TITLE_DESC: Int = 1
        const val SORT_DATE_ASC: Int = 2
        const val SORT_DATE_DESC: Int = 3

        /**
         * The singleton instance of Controller class
         */
        var instance: Controller? = null

        /**
         * Instantiates the singleton instance of Controller class
         *
         * @param context the application context
         */
        fun create(context: Context) {
            instance = Controller(context)
        }
    }
}
