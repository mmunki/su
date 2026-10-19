package su.android.utils

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * Reveals or hides an overlay panel while travelling its toggle button between
 * the corner of the screen and the center of the panel.
 *
 * This used to be a circular reveal animation built on the Material library; it
 * is now a plain fade plus a translation, which fits the flat, classic look of
 * the rest of the UI.
 */
object MotionRevealHelper {

    private const val DURATION = 180L

    fun withViews(revealable: View, fab: View, expanded: Boolean) {
        if (expanded) {
            revealable.animate().cancel()
            revealable.isVisible = true
            revealable.alpha = 0f
            revealable.animate()
                .alpha(1f)
                .setDuration(DURATION)
                .start()
        } else {
            revealable.animate().cancel()
            revealable.alpha = 1f
            revealable.isInvisible = true
        }

        val parent = fab.parent as? View
        val targetX = if (expanded && parent != null)
            (parent.width - fab.width) / 2f - fab.left - fab.translationX
        else 0f
        val targetY = if (expanded && parent != null)
            (parent.height - fab.height) / 2f - fab.top - fab.translationY
        else 0f

        fab.animate()
            .translationX(targetX)
            .translationY(targetY)
            .setDuration(DURATION)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }
}
