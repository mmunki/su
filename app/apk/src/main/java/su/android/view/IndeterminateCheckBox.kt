package su.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Checkbox

/**
 * Miuix tri state checkbox: checked, unchecked or indeterminate.
 *
 * This is the Compose Miuix [Checkbox] in a small host so the data binding
 * `state` adapter keeps working. The previous version was an AppCompat
 * checkbox with a square Holo drawable, which looked out of place next to the
 * Miuix switches and cards.
 */
class IndeterminateCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var checkedState by mutableStateOf<Boolean?>(false)
    private var enabledState by mutableStateOf(true)
    private var stateChangedListener: ((IndeterminateCheckBox, Boolean?) -> Unit)? = null

    private val composeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        setContent {
            MiuixControlTheme {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Checkbox(
                        state = when (checkedState) {
                            null -> ToggleableState.Indeterminate
                            true -> ToggleableState.On
                            false -> ToggleableState.Off
                        },
                        onClick = { setStateFromUser(nextState()) },
                        enabled = enabledState,
                    )
                }
            }
        }
    }

    init {
        isClickable = true
        isFocusable = true
        minimumWidth = dp(48)
        minimumHeight = dp(48)
        setPadding(0, dp(6), 0, dp(6))
        addView(composeView)
    }

    /** `true`, `false` or `null` for the indeterminate state. */
    var state: Boolean?
        get() = checkedState
        set(value) {
            if (checkedState != value) checkedState = value
        }

    fun setOnStateChangedListener(listener: ((IndeterminateCheckBox, Boolean?) -> Unit)?) {
        stateChangedListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
        composeView.isEnabled = enabled
    }

    override fun performClick(): Boolean {
        if (!isEnabled) return false
        setStateFromUser(nextState())
        return super.performClick()
    }

    private fun nextState(): Boolean? = when (checkedState) {
        null -> true
        true -> false
        false -> true
    }

    private fun setStateFromUser(value: Boolean?) {
        if (checkedState != value) {
            checkedState = value
            stateChangedListener?.invoke(this, value)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
