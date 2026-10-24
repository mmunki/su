package su.android.ui.theme

import su.android.R

enum class Theme(
    val themeName: String,
    val themeRes: Int
) {

    Miuix(
        themeName = "Miuix",
        themeRes = R.style.ThemeFoundationBase_Miuix
    );

    val isSelected get() = true

    companion object {
        val selected get() = Miuix
    }

}
