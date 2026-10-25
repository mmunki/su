package su.android.view

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import su.android.R
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState
import top.yukonga.miuix.kmp.basic.SnackbarResult

/**
 * Miuix snackbar. The old implementation inflated a fixed dark Holo bar, so it
 * ignored the light theme and did not look like the rest of the app. This
 * implementation hosts the official Miuix [SnackbarHost] in a small
 * [ComposeView] and keeps the same builder API the activities already use.
 */
class Snackbar private constructor(
    private val parent: ViewGroup,
    private val duration: Int,
    initialMessage: CharSequence,
) {

    private val message = mutableStateOf(initialMessage)
    private val actionText = mutableStateOf<CharSequence?>(null)
    private var actionListener: View.OnClickListener? = null

    private val composeView = ComposeView(parent.context)
    private var anchor: View? = null
    private var isShown = false

    fun setText(message: CharSequence): Snackbar {
        this.message.value = message
        return this
    }

    fun setAction(text: CharSequence?, listener: View.OnClickListener?): Snackbar {
        actionText.value = text
        actionListener = listener
        return this
    }

    fun setAnchorView(anchor: View?): Snackbar {
        this.anchor = anchor
        return this
    }

    fun show(): Snackbar {
        if (isShown) return this
        isShown = true

        composeView.setContent {
            MiuixControlTheme {
                val hostState = remember { SnackbarHostState() }
                val currentMessage by message
                val currentAction by actionText

                LaunchedEffect(currentMessage, currentAction) {
                    val result = hostState.showSnackbar(
                        message = currentMessage.toString(),
                        actionLabel = currentAction?.toString(),
                        duration = when (duration) {
                            LENGTH_LONG -> SnackbarDuration.Long
                            LENGTH_INDEFINITE -> SnackbarDuration.Indefinite
                            else -> SnackbarDuration.Short
                        },
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        actionListener?.onClick(composeView)
                    }
                    parent.post { dismiss() }
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    SnackbarHost(
                        state = hostState,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        parent.addView(composeView, createLayoutParams())
        return this
    }

    fun dismiss() {
        if (!isShown) return
        isShown = false
        parent.removeView(composeView)
    }

    private fun createLayoutParams(): ViewGroup.LayoutParams {
        val margin = composeView.resources.getDimensionPixelSize(R.dimen.l1)
        val bottom = anchor?.let { anchorTop(it) } ?: 0

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
        params.setMargins(0, 0, 0, bottom + margin)
        return params
    }

    private fun anchorTop(anchor: View): Int {
        val parentLocation = IntArray(2)
        val anchorLocation = IntArray(2)
        parent.getLocationOnScreen(parentLocation)
        anchor.getLocationOnScreen(anchorLocation)
        return parentLocation[1] + parent.height - anchorLocation[1]
    }

    companion object {

        const val LENGTH_SHORT = 0
        const val LENGTH_LONG = 1
        const val LENGTH_INDEFINITE = 2

        @JvmStatic
        fun make(view: View, message: CharSequence, duration: Int): Snackbar {
            val parent = findParent(view)
            return Snackbar(parent, duration, message)
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
