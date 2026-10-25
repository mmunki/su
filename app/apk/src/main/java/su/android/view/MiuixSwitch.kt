package su.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import top.yukonga.miuix.kmp.basic.Switch

/**
 * A drop-in replacement for [androidx.appcompat.widget.SwitchCompat] that renders the
 * official Miuix [Switch] in a small Compose host.
 *
 * The public API mirrors the parts of a compound button that the app and data binding
 * use: [isChecked], [setChecked], [setOnCheckedChangeListener] and [performClick].
 */
class MiuixSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    fun interface OnCheckedChangeListener {
        fun onCheckedChanged(view: MiuixSwitch, isChecked: Boolean)
    }

    private var checkedState by mutableStateOf(false)
    private var enabledState by mutableStateOf(true)

    private var changeListener: OnCheckedChangeListener? = null

    private val composeView = ComposeView(context).apply {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        setContent {
            MiuixControlTheme {
                Switch(
                    checked = checkedState,
                    enabled = enabledState,
                    onCheckedChange = { setCheckedFromUser(it) },
                )
            }
        }
    }

    init {
        isClickable = true
        isFocusable = true
        minimumWidth = dp(49)
        minimumHeight = dp(48)
        setPadding(0, dp(10), 0, dp(10))
        addView(composeView)
    }

    val isChecked: Boolean get() = checkedState

    fun setChecked(checked: Boolean) {
        if (checkedState != checked) checkedState = checked
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        changeListener = listener
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
        composeView.isEnabled = enabled
    }

    override fun performClick(): Boolean {
        if (!isEnabled) return false
        setCheckedFromUser(!checkedState)
        return super.performClick()
    }

    private fun setCheckedFromUser(checked: Boolean) {
        if (checkedState != checked) {
            checkedState = checked
            changeListener?.onCheckedChanged(this, checked)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
