package github.yaa110.memento.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import github.yaa110.memento.App
import github.yaa110.memento.R
import github.yaa110.memento.inner.Animator

class Fab : AppCompatImageView {
    private var isHidden = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * Makes the fab visible if it is hidden
     */
    fun show() {
        if (isHidden) {
            isHidden = false
            Animator.Companion.create(context.applicationContext)
                .on<Fab>(this)
                .setStartVisibility(VISIBLE)
                .animate(R.anim.fab_scroll_in)
        }
    }

    /**
     * Makes the fab gone if it is visible and smart fab preference is enabled
     */
    fun hide() {
        if (App.Companion.smartFab && !isHidden) {
            isHidden = true
            Animator.Companion.create(context.applicationContext)
                .on<Fab>(this)
                .setEndVisibility(GONE)
                .animate(R.anim.fab_scroll_out)
        }
    }
}
