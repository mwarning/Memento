package github.yaa110.memento.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import github.yaa110.memento.App

class FixedHeightRecyclerView : RecyclerView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var heightSpec = heightSpec
        heightSpec =
            MeasureSpec.makeMeasureSpec(App.Companion.DEVICE_HEIGHT / 2, MeasureSpec.EXACTLY)
        super.onMeasure(widthSpec, heightSpec)
    }
}
