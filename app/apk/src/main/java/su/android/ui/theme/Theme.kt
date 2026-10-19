package su.android.ui.theme

import su.android.R

enum class Theme(
    val themeName: String,
    val themeRes: Int
) {

    Holo(
        themeName = "Holo Dark",
        themeRes = R.style.ThemeFoundationMD3_Holo
    );

    val isSelected get() = true

    companion object {
        val selected get() = Holo
    }

}
