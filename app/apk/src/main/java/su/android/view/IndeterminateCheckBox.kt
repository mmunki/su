package su.android.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatCheckBox
import su.android.R

/**
 * Square, flat tri state checkbox: checked, unchecked or indeterminate.
 *
 * This replaces `com.github.topjohnwu:indeterminate-checkbox`, which was built
 * on top of the Google Material library and therefore cannot be used anymore.
 */
class IndeterminateCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.checkboxStyle
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    private var indeterminate = false
    private var stateChangedListener: ((IndeterminateCheckBox, Boolean?) -> Unit)? = null

    init {
        syncButtonDrawable()
    }

    /** `true`, `false` or `null` for the indeterminate state. */
    var state: Boolean?
        get() = if (indeterminate) null else isChecked
        set(value) {
            indeterminate = value == null
            isChecked = value ?: false
            syncButtonDrawable()
        }

    fun setOnStateChangedListener(listener: (IndeterminateCheckBox, Boolean?) -> Unit) {
        stateChangedListener = listener
    }

    override fun performClick(): Boolean {
        if (indeterminate) {
            // Tapping an indeterminate group selects everything.
            indeterminate = false
            isChecked = true
            syncButtonDrawable()
            stateChangedListener?.invoke(this, state)
            return true
        }

        val handled = super.performClick()
        stateChangedListener?.invoke(this, state)
        return handled
    }

    private fun syncButtonDrawable() {
        val res = if (indeterminate) R.drawable.ic_checkbox_indeterminate else R.drawable.foundation_checkbox
        setButtonDrawable(AppCompatResources.getDrawable(context, res))
    }
}
