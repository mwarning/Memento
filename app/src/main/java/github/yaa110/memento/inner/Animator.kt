package github.yaa110.memento.inner

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils

@Suppress("unused")
class Animator private constructor(private val context: Context?) {
    private var view: View? = null
    private var clear = true
    private var delay: Long = 0
    private var start_visiblity = View.VISIBLE
    private var end_visiblity = View.VISIBLE
    private var listener: AnimatorListener? = null

    fun <T : View?> on(view: T): Animator {
        this.view = view
        return this
    }

    fun setDelay(delay: Long): Animator {
        this.delay = delay
        return this
    }

    fun setClear(clear: Boolean): Animator {
        this.clear = clear
        return this
    }

    fun setStartVisibility(visiblity: Int): Animator {
        this.start_visiblity = visiblity
        return this
    }

    fun setEndVisibility(visiblity: Int): Animator {
        this.end_visiblity = visiblity
        return this
    }

    fun setListener(listener: AnimatorListener?): Animator {
        this.listener = listener
        return this
    }

    fun animate(anim_id: Int) {
        val animation = AnimationUtils.loadAnimation(context, anim_id)
        if (delay > 0) {
            animation.startOffset = delay
        }
        animation.setAnimationListener(object : Animation.AnimationListener {
            var end_status: Boolean = false

            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                if (!end_status) {
                    end_status = true
                    if (clear) view!!.clearAnimation()
                    view!!.visibility = end_visiblity
                    if (listener != null) {
                        listener!!.onAnimate()
                    }
                }
            }

            override fun onAnimationRepeat(animation: Animation) {
            }
        })
        view!!.visibility = start_visiblity
        view!!.startAnimation(animation)
    }

    interface AnimatorListener {
        fun onAnimate()
    }

    companion object {
        fun create(context: Context?): Animator {
            return Animator(context)
        }
    }
}
