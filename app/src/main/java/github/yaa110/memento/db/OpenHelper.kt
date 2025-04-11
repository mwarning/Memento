package github.yaa110.memento.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OpenHelper(context: Context?) :
    SQLiteOpenHelper(context, NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // Main table to store notes and categories
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PARENT_ID + " INTEGER DEFAULT -1, " +
                    COLUMN_TITLE + " TEXT DEFAULT '', " +
                    COLUMN_BODY + " TEXT DEFAULT '', " +
                    COLUMN_TYPE + " INTEGER DEFAULT 0, " +
                    COLUMN_ARCHIVED + " INTEGER DEFAULT 0, " +
                    COLUMN_THEME + " INTEGER DEFAULT 0, " +
                    COLUMN_COUNTER + " INTEGER DEFAULT 0, " +
                    COLUMN_DATE + " TEXT DEFAULT '', " +
                    COLUMN_EXTRA + " TEXT DEFAULT ''" +
                    ")"
        )

        // Undo table to make delete queries restorable
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS " + TABLE_UNDO + " (" +
                    COLUMN_SQL + " TEXT" +
                    ")"
        )

        // A trigger to empty UNDO table, add restoring sql query to UNDO table, then delete all child notes before deleting the parent note
        db.execSQL(
            "CREATE TRIGGER IF NOT EXISTS _t1_dn BEFORE DELETE ON " + TABLE_NOTES + " BEGIN " +
                    "INSERT INTO " + TABLE_UNDO + " VALUES('INSERT INTO " + TABLE_NOTES +
                    "(" + COLUMN_ID + "," + COLUMN_PARENT_ID + "," + COLUMN_TITLE + "," + COLUMN_BODY + "," + COLUMN_TYPE + "," + COLUMN_ARCHIVED + "," + COLUMN_THEME + "," + COLUMN_COUNTER + "," + COLUMN_DATE + "," + COLUMN_EXTRA + ")" +
                    "VALUES('||old." + COLUMN_ID + "||','||old." + COLUMN_PARENT_ID + "||','||quote(old." + COLUMN_TITLE + ")||','||quote(old." + COLUMN_BODY + ")||','||old." + COLUMN_TYPE + "||','||old." + COLUMN_ARCHIVED + "||','||old." + COLUMN_THEME + "||','||old." + COLUMN_COUNTER + "||','||quote(old." + COLUMN_DATE + ")||','||quote(old." + COLUMN_EXTRA + ")||')'); END"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        const val TABLE_NOTES: String = "notes"
        const val TABLE_UNDO: String = "undo"
        const val COLUMN_ID: String = "_id"
        const val COLUMN_TITLE: String = "_title"
        const val COLUMN_BODY: String = "_body"
        const val COLUMN_TYPE: String = "_type"
        const val COLUMN_DATE: String = "_date"
        const val COLUMN_ARCHIVED: String = "_archived"
        const val COLUMN_THEME: String = "_theme"
        const val COLUMN_COUNTER: String = "_counter"
        const val COLUMN_PARENT_ID: String = "_parent"
        const val COLUMN_EXTRA: String = "_extra"
        const val COLUMN_SQL: String = "_sql"
        private const val VERSION = 1
        private const val NAME = "data.db"
    }
}
