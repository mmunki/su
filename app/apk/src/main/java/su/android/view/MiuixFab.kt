package su.android.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt

/**
 * A Miuix floating action button that keeps the classic [android.widget.ImageButton]
 * workflow used by the XML layouts: `android:src`, [android:onClick] and the normal
 * layout parameters all continue to work.
 */
open class MiuixFab @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var iconRes by mutableIntStateOf(0)
    private var iconPainter by mutableStateOf<Painter?>(null)
    private val composeView = ComposeView(context)

    init {
        background = null
        isClickable = true
        isFocusable = true

        val typed = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.src))
        iconRes = typed.getResourceId(0, 0)
        typed.recycle()

        composeView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        composeView.setContent {
            MiuixControlTheme {
                FloatingActionButton(
                    onClick = { performClick() },
                    modifier = Modifier.size(60.dp),
                ) {
                    val resourcePainter = remember(iconRes) {
                        if (iconRes == 0) null
                        else runCatching { loadDrawablePainter(iconRes) }.getOrNull()
                    }
                    val icon = iconPainter ?: resourcePainter
                    if (icon != null) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MiuixTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
        addView(composeView)
    }

    fun setIconResource(@DrawableRes res: Int) {
        iconPainter = null
        iconRes = res
    }

    fun setIconDrawable(drawable: Drawable?) {
        iconRes = 0
        iconPainter = drawable?.let { BitmapPainter(it.toBitmap().asImageBitmap()) }
    }

    private fun loadDrawablePainter(res: Int): Painter? {
        val drawable = AppCompatResources.getDrawable(context, res) ?: return null
        val size = (24 * resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
        return BitmapPainter(drawable.toBitmap(size, size).asImageBitmap())
    }
}
