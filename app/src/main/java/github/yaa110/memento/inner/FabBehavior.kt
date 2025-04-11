package github.yaa110.memento.inner

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import github.yaa110.memento.widget.Fab
import kotlin.math.min

@Suppress("unused")
class FabBehavior(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<Fab>() {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: Fab, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: Fab,
        dependency: View
    ): Boolean {
        if (dependency is SnackbarLayout) {
            val translationY = min(
                0.0,
                (dependency.getTranslationY() - dependency.getHeight()).toDouble()
            ).toFloat()
            child.translationY = translationY
            return true
        }
        return false
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Fab,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int
    ): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(
                    coordinatorLayout, child, directTargetChild, target,
                    nestedScrollAxes
                )
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: Fab,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed
        )

        if (dyConsumed > 0) {
            child.hide()
        } else if (dyConsumed < 0) {
            child.show()
        }
    }
}
