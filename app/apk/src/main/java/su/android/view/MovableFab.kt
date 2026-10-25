package su.android.view

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

/**
 * A [MiuixFab] that can be dragged anywhere inside its parent.
 *
 * A simple tap is still dispatched as a regular click, only touch sequences
 * that move further than the touch slop turn into a drag.  The position is
 * remembered across configuration changes, and the button snaps to the
 * nearest horizontal edge when the finger is lifted so it never covers the
 * list it belongs to.
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return super.dispatchTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                downTranslationX = translationX
                downTranslationY = translationY
                dragging = false
                isPressed = true
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downRawX
                val dy = event.rawY - downRawY
                if (!dragging && (dx * dx + dy * dy) > (touchSlop * touchSlop)) {
                    dragging = true
                    isPressed = false
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (dragging) {
                    translationX = clampX(downTranslationX + dx)
                    translationY = clampY(downTranslationY + dy)
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val wasDragging = dragging
                dragging = false
                isPressed = false
                parent?.requestDisallowInterceptTouchEvent(false)
                if (wasDragging) {
                    snapToEdge()
                } else {
                    performClick()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                dragging = false
                isPressed = false
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        translationX = clampX(translationX)
        translationY = clampY(translationY)
    }

    private fun snapToEdge() {
        val parentWidth = (parent as? View)?.width ?: return
        val center = left + width / 2f + translationX
        val target = if (center < parentWidth / 2f) minTranslationX() else maxTranslationX()
        animate()
            .translationX(target)
            .setDuration(180L)
            .start()
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

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).also {
            it.translationX = translationX
            it.translationY = translationY
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            translationX = state.translationX
            translationY = state.translationY
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private class SavedState : BaseSavedState {
        var translationX = 0f
        var translationY = 0f

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            translationX = source.readFloat()
            translationY = source.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(translationX)
            out.writeFloat(translationY)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }
}
