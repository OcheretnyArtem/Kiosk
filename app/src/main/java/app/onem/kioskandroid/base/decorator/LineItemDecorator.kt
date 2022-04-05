package app.onem.kioskandroid.base.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntDef
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView

@Suppress("LongParameterList")
open class LineItemDecorator(
    context: Context,
    @ColorRes lineColor: Int,
    @DimenRes lineWidthId: Int,
    @DimenRes startMarginId: Int = 0,
    @DimenRes endMarginId: Int = 0,
    @LineDecoratorOrientation protected val orientation: Int = VERTICAL,
    val isLastItemWithLine: Boolean = true,
    private val isFirstItemWithLine: Boolean = false
) : RecyclerView.ItemDecoration() {

    companion object {

        @IntDef(VERTICAL, HORIZONTAL)
        @Retention(AnnotationRetention.SOURCE)
        annotation class LineDecoratorOrientation

        const val VERTICAL = 0

        const val HORIZONTAL = 1
    }

    @Px
    private val lineWidth: Int = context.resources.getDimensionPixelOffset(lineWidthId)

    @Px
    protected val startMargin: Int =
        if (startMarginId != 0) context.resources.getDimensionPixelOffset(startMarginId) else 0

    @Px
    protected val endMargin: Int =
        if (endMarginId != 0) context.resources.getDimensionPixelOffset(endMarginId) else 0

    protected val paint = Paint().apply {
        strokeWidth = lineWidth.toFloat()
        color = ContextCompat.getColor(context, lineColor)
        isAntiAlias = true
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (orientation == VERTICAL) {
            outRect.bottom = lineWidth
            if (isFirstItemWithLine && parent.getChildAdapterPosition(view) == 0) {
                outRect.top = lineWidth
            }
        } else {
            outRect.right = lineWidth
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (orientation == VERTICAL) {
            drawHorizontalLine(canvas, parent)
        } else {
            drawVerticalLine(canvas, parent)
        }
    }

    protected open fun drawHorizontalLine(canvas: Canvas, parent: RecyclerView) {
        parent.forEachIndexed { position, child ->
            if (position == parent.size) return@forEachIndexed
            if (position == parent.childCount - 1 && !isLastItemWithLine) return@forEachIndexed
            val params = child.layoutParams as RecyclerView.LayoutParams
            var top = child.bottom + params.bottomMargin.toFloat()
            canvas.drawLine(
                startMargin.toFloat(),
                top,
                parent.width - endMargin.toFloat(),
                top,
                paint
            )

            if (isFirstItemWithLine && position == 0) {
                top = child.top + params.topMargin.toFloat()
                canvas.drawLine(
                    startMargin.toFloat(),
                    top,
                    parent.width - endMargin.toFloat(),
                    top,
                    paint
                )
            }
        }
    }

    protected open fun drawVerticalLine(canvas: Canvas, parent: RecyclerView) {
        parent.forEachIndexed { position, child ->
            if (position == parent.size - 1) return@forEachIndexed
            val params = child.layoutParams as RecyclerView.LayoutParams
            val x = child.right + params.marginEnd.toFloat()

            canvas.drawLine(
                x,
                child.top.toFloat(),
                x,
                child.bottom.toFloat(),
                paint
            )
        }
    }
}
