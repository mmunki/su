package su.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * A [MiuixFab] that can be dragged anywhere inside its parent.
 *
 * A simple tap is still dispatched as a regular click, only touch sequences
 * that move further than the touch slop turn into a drag.
 */
class MovableFab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MiuixFab(context, attrs) {

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val edgeMargin = (resources.displayMetrics.density * 8).toInt()

    private var downRawX = 0f
    private var downRawY = 0f
    private var downTranslationX = 0f
    private var downTranslationY = 0f
    private var dragging = false

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                downTranslationX = translationX
                downTranslationY = translationY
                dragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && (dx * dx + dy * dy) > (touchSlop * touchSlop)) {
                    dragging = true
                    isPressed = false
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> dragging = false
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    translationX = clampX(downTranslationX + event.rawX - downRawX)
                    translationY = clampY(downTranslationY + event.rawY - downRawY)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val wasDragging = dragging
                dragging = false
                if (wasDragging) {
                    isPressed = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    /** Minimum translation along X keeping the button inside its parent. */
    private fun minTranslationX() = (-left + edgeMargin).toFloat()

    /** Maximum translation along X keeping the button inside its parent. */
    private fun maxTranslationX() = ((parent as? View)?.width ?: 0) - right - edgeMargin.toFloat()

    private fun minTranslationY() = (-top + edgeMargin).toFloat()

    private fun maxTranslationY() = ((parent as? View)?.height ?: 0) - bottom - edgeMargin.toFloat()

    private fun clampX(value: Float): Float {
        val lo = minTranslationX()
        val hi = maxTranslationX()
        return value.coerceIn(minOf(lo, hi), maxOf(lo, hi))
    }

    private fun clampY(value: Float): Float {
        val lo = minTranslationY()
        val hi = maxTranslationY()
        return value.coerceIn(minOf(lo, hi), maxOf(lo, hi))
    }
}
