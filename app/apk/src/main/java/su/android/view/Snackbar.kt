package su.android.view

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import su.android.R

/**
 * Drop in replacement for the old Material snackbar, using a plain, square Holo
 * styled bar that is attached to the closest [FrameLayout] or
 * [CoordinatorLayout] above the given view.
 */
class Snackbar private constructor(
    private val parent: ViewGroup,
    private val layout: View,
    private val duration: Int
) {

    private val handler = Handler(Looper.getMainLooper())
    private val dismissRunnable = Runnable { dismiss() }

    private var anchor: View? = null
    private var isShown = false

    private val text: TextView get() = layout.findViewById(R.id.snackbar_text)
    private val action: Button get() = layout.findViewById(R.id.snackbar_action)

    fun setText(message: CharSequence): Snackbar {
        text.text = message
        return this
    }

    fun setAction(text: CharSequence?, listener: View.OnClickListener?): Snackbar {
        val action = action
        if (text == null) {
            action.isVisible = false
        } else {
            action.text = text
            action.isVisible = true
            action.setOnClickListener {
                listener?.onClick(it)
                dismiss()
            }
        }
        return this
    }

    fun setAnchorView(anchor: View?): Snackbar {
        this.anchor = anchor
        return this
    }

    fun show(): Snackbar {
        if (isShown) return this
        isShown = true

        parent.addView(layout, createLayoutParams())
        layout.alpha = 0f
        layout.translationY = layout.resources.displayMetrics.density * 16
        layout.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(ANIMATION_DURATION)
            .start()

        if (duration != LENGTH_INDEFINITE) {
            handler.postDelayed(dismissRunnable, if (duration == LENGTH_LONG) 3500L else 2000L)
        }
        return this
    }

    fun dismiss() {
        handler.removeCallbacks(dismissRunnable)
        if (!isShown) return
        isShown = false
        layout.animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION)
            .withEndAction { parent.removeView(layout) }
            .start()
    }

    private fun createLayoutParams(): ViewGroup.LayoutParams {
        val margin = layout.resources.getDimensionPixelSize(R.dimen.l1)
        val bottom = anchor?.let { anchorTop() + it.height } ?: 0

        val params: ViewGroup.MarginLayoutParams = when (parent) {
            is CoordinatorLayout ->
                CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.gravity = Gravity.BOTTOM }
            is FrameLayout ->
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.gravity = Gravity.BOTTOM }
            is LinearLayout ->
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.gravity = Gravity.BOTTOM }
            else -> ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        params.setMargins(margin, 0, margin, bottom + margin)
        return params
    }

    private fun anchorTop(): Int {
        val anchor = anchor ?: return 0
        val parentLocation = IntArray(2)
        val anchorLocation = IntArray(2)
        parent.getLocationOnScreen(parentLocation)
        anchor.getLocationOnScreen(anchorLocation)
        return parentLocation[1] + parent.height - anchorLocation[1] - anchor.height
    }

    companion object {

        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1
        const val LENGTH_INDEFINITE = 2

        private const val ANIMATION_DURATION = 180L

        @JvmStatic
        fun make(view: View, message: CharSequence, duration: Int): Snackbar {
            val parent = findParent(view)
            val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.snackbar, parent, false)
                .also { it.findViewById<TextView>(R.id.snackbar_text).text = message }
            return Snackbar(parent, layout, duration)
        }

        /** Walks up the hierarchy looking for a container that can host an overlay. */
        private fun findParent(view: View): ViewGroup {
            var current: View? = view
            while (current != null) {
                if (current is FrameLayout || current is CoordinatorLayout) return current
                current = current.parent as? View
            }
            val root = view.rootView
            (root.findViewById<ViewGroup>(android.R.id.content))?.let { return it }
            return root as? ViewGroup ?: throw IllegalStateException("No suitable parent for Snackbar")
        }
    }
}
