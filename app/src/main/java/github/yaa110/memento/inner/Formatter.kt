package github.yaa110.memento.inner

import android.text.format.DateFormat

object Formatter {
    private const val DATE_FORMAT = "E, LLLL d, yyyy"
    private const val SHORT_DATE_FORMAT = "LLL d, yyyy"

    @JvmOverloads
    fun formatDate(millis: Long = System.currentTimeMillis()): CharSequence {
        return DateFormat.format(DATE_FORMAT, millis)
    }

    fun formatShortDate(millis: Long): CharSequence {
        return DateFormat.format(SHORT_DATE_FORMAT, millis)
    }
}
