package github.yaa110.memento.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

class Drawer {
    var type: Int = 0

    @DrawableRes
    var resId: Int = 0

    @StringRes
    var title: Int = 0

    constructor()

    constructor(type: Int, @DrawableRes resId: Int, @StringRes title: Int) {
        this.type = type
        this.resId = resId
        this.title = title
    }

    companion object {
        const val TYPE_SPLITTER: Int = 0
        const val TYPE_ABOUT: Int = 1
        const val TYPE_BACKUP: Int = 2
        const val TYPE_RESTORE: Int = 3
        const val TYPE_SETTINGS: Int = 4

        fun divider(): Drawer {
            val splitter = Drawer()
            splitter.type = TYPE_SPLITTER
            return splitter
        }
    }
}
