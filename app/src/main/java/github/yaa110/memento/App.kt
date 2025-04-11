// In the name of Allah
/* ----------------------------------- */
package github.yaa110.memento

import android.app.Application
import android.content.SharedPreferences
import android.graphics.Point
import android.view.WindowManager
import github.yaa110.memento.db.Controller

class App : Application() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        // Get preferences
        prefs = getSharedPreferences(packageName, MODE_PRIVATE)
        smartFab = prefs.getBoolean(SMART_FAB_KEY, true)
        sortCategoriesBy =
            sanitizeSort(prefs.getInt(SORT_CATEGORIES_KEY, Controller.SORT_DATE_DESC))
        sortNotesBy =
            sanitizeSort(prefs.getInt(SORT_NOTES_KEY, Controller.SORT_DATE_DESC))
        last_path = prefs.getString(LAST_PATH_KEY, null)

        // Setup database controller
        Controller.create(applicationContext)

        val size = Point()
        (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(size)
        DEVICE_HEIGHT = size.y

        instance = this
    }

    private fun sanitizeSort(sortId: Int): Int {
        if (sortId < 0 || sortId > 3) return Controller.SORT_DATE_DESC
        return sortId
    }

    fun putPrefs(key: String?, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun putPrefs(key: String?, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    companion object {
        const val BACKUP_EXTENSION: String = "mem"

        /* Preferences' Keys */
        const val SMART_FAB_KEY: String = "a1"
        const val SORT_CATEGORIES_KEY: String = "a2"
        const val SORT_NOTES_KEY: String = "a3"
        const val LAST_PATH_KEY: String = "a4"
        var instance: App? = null
        var DEVICE_HEIGHT: Int = 0

        /* Preferences */
        var smartFab: Boolean = false
        var sortCategoriesBy: Int = 0
        var sortNotesBy: Int = 0
        var last_path: String? = null
    }
}
