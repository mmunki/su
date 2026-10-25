package su.android.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import su.android.R
import kotlin.math.roundToInt

/**
 * A Miuix styled button that keeps the classic [android.widget.Button] workflow used by
 * the XML layouts: [android:text], `app:icon`, [android:onClick], enabled state and the
 * normal layout parameters all continue to work.
 */
class MiuixButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private var label by mutableStateOf("")
    private var iconRes by mutableIntStateOf(0)
    private var iconPainter by mutableStateOf<Painter?>(null)
    private var enabledState by mutableStateOf(true)
    private var selectedState by mutableStateOf(false)
    private var textSizeSp by mutableStateOf(0f)

    private var textColors: ColorStateList? = null
    private var textColorDefault = 0
    private var touchListener: OnTouchListener? = null

    private val style: Int
    private val composeView = ComposeView(context)

    init {
        background = null
        isClickable = true
        isFocusable = true

        val typed = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.text))
        label = typed.getText(0)?.toString().orEmpty()
        typed.recycle()

        val drawable = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.drawableStart))
        iconRes = drawable.getResourceId(0, 0)
        drawable.recycle()

        val styled = context.obtainStyledAttributes(attrs, R.styleable.MiuixButton)
        style = styled.getInt(R.styleable.MiuixButton_miuixButtonStyle, STYLE_FILLED)
        styled.recycle()

        val size = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textSize))
        val scaledDensity = resources.displayMetrics.density * resources.configuration.fontScale
        textSizeSp = if (scaledDensity > 0f) size.getDimension(0, 0f) / scaledDensity else 0f
        size.recycle()

        attrs?.getAttributeValue(ANDROID_NS, "textColor")?.let { _ ->
            val color = context.obtainStyledAttributes(attrs, intArrayOf(android.R.attr.textColor))
            textColors = color.getColorStateList(0)
            textColorDefault = textColors?.defaultColor ?: 0
            color.recycle()
        }

        composeView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        composeView.setContent {
            MiuixControlTheme {
                val fillWidth = layoutParams?.width == LayoutParams.MATCH_PARENT ||
                    layoutParams?.width == 0
                val modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier
                val customColor = textColors?.getColorForState(currentState(), textColorDefault)
                    ?.let { Color(it) }

                val colors = when (style) {
                    STYLE_TEXT -> ButtonDefaults.buttonColors(
                        color = Color.Transparent,
                        disabledColor = Color.Transparent,
                        contentColor = customColor ?: MiuixTheme.colorScheme.primary,
                        disabledContentColor = MiuixTheme.colorScheme.disabledOnSurface,
                    )
                    STYLE_OUTLINED -> ButtonDefaults.buttonColors(
                        color = Color.Transparent,
                        disabledColor = Color.Transparent,
                        contentColor = customColor ?: MiuixTheme.colorScheme.primary,
                        disabledContentColor = MiuixTheme.colorScheme.disabledOnSurface,
                    )
                    else -> ButtonDefaults.buttonColorsPrimary().let {
                        if (customColor != null) it.copy(contentColor = customColor) else it
                    }
                }

                Button(
                    onClick = { performClick() },
                    modifier = modifier,
                    enabled = enabledState,
                    colors = colors,
                    insideMargin = if (style == STYLE_TEXT) {
                        PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    } else {
                        ButtonDefaults.InsideMargin
                    },
                ) {
                    val resourcePainter = remember(iconRes, selectedState, enabledState) {
                        if (iconRes == 0) null
                        else runCatching { loadDrawablePainter(iconRes) }.getOrNull()
                    }
                    val icon = iconPainter ?: resourcePainter
                    if (icon != null) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(18.dp),
                            tint = LocalContentColor.current,
                        )
                    }
                    Text(
                        text = label,
                        style = MiuixTheme.textStyles.button,
                        fontSize = if (textSizeSp > 0f) textSizeSp.sp else TextUnit.Unspecified,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        addView(composeView)
    }

    val text: CharSequence get() = label

    fun setText(value: CharSequence?) {
        label = value?.toString().orEmpty()
    }

    fun setText(@StringRes value: Int) {
        label = if (value == 0) "" else context.getText(value).toString()
    }

    fun setIconResource(@DrawableRes res: Int) {
        iconPainter = null
        iconRes = res
    }

    fun setIconDrawable(drawable: Drawable?) {
        iconRes = 0
        iconPainter = drawable?.let { BitmapPainter(it.toBitmap().asImageBitmap()) }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
        composeView.isEnabled = enabled
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        selectedState = selected
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        touchListener = l
        super.setOnTouchListener(l)
        composeView.setOnTouchListener { view, event ->
            touchListener?.onTouch(view, event) ?: false
        }
    }

    private fun currentState(): IntArray = intArrayOf(
        if (selectedState) android.R.attr.state_selected else -android.R.attr.state_selected,
        if (enabledState) android.R.attr.state_enabled else -android.R.attr.state_enabled,
    )

    /**
     * Material/Miuix painters only understand vector or raster resources. The app also
     * uses state-list drawables (for example the log icon), so resolve the drawable with
     * the platform and hand Compose a bitmap instead of letting painterResource throw.
     */
    private fun loadDrawablePainter(res: Int): Painter? {
        val drawable = AppCompatResources.getDrawable(context, res)?.mutate() ?: return null
        drawable.state = currentState()
        val size = (24 * resources.displayMetrics.density).roundToInt().coerceAtLeast(1)
        return BitmapPainter(drawable.toBitmap(size, size).asImageBitmap())
    }

    companion object {
        const val STYLE_FILLED = 0
        const val STYLE_TEXT = 1
        const val STYLE_OUTLINED = 2

        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
