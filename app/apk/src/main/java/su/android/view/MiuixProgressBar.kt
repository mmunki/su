package su.android.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import su.android.R
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator

/**
 * Drop in replacement for [android.widget.ProgressBar] that renders the
 * official Miuix progress indicators. Both the horizontal and the circular
 * variants understand `android:progress`, `android:max` and
 * `android:indeterminate`, so existing data bindings keep working.
 */
class MiuixProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var progressValue by mutableIntStateOf(0)
    private var maxValue by mutableIntStateOf(100)
    private var indeterminateState by mutableStateOf(false)
    private var circular by mutableStateOf(false)

    private val composeView = ComposeView(context)

    init {
        val typed = context.obtainStyledAttributes(
            attrs,
            intArrayOf(
                android.R.attr.progress,
                android.R.attr.max,
                android.R.attr.indeterminate,
                R.attr.miuixProgressStyle,
            )
        )
        progressValue = typed.getInt(0, 0)
        maxValue = typed.getInt(1, 100)
        indeterminateState = typed.getBoolean(2, false)
        circular = typed.getInt(3, STYLE_LINEAR) == STYLE_CIRCULAR
        typed.recycle()

        composeView.layoutParams = if (circular) {
            LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        } else {
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        composeView.setContent {
            MiuixControlTheme {
                if (circular) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = if (indeterminateState) null else fraction(),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = if (indeterminateState) null else fraction(),
                        )
                    }
                }
            }
        }
        addView(composeView)
    }

    var progress: Int
        get() = progressValue
        set(value) {
            progressValue = value
        }

    var max: Int
        get() = maxValue
        set(value) {
            maxValue = value
        }

    fun setIndeterminate(value: Boolean) {
        indeterminateState = value
    }

    private fun fraction(): Float {
        if (maxValue <= 0) return 0f
        return (progressValue.toFloat() / maxValue).coerceIn(0f, 1f)
    }

    companion object {
        const val STYLE_LINEAR = 0
        const val STYLE_CIRCULAR = 1
    }
}
