package su.android.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import su.android.core.Config

internal fun currentMiuixColorSchemeMode(): ColorSchemeMode = when (Config.darkTheme) {
    AppCompatDelegate.MODE_NIGHT_NO -> ColorSchemeMode.Light
    AppCompatDelegate.MODE_NIGHT_YES -> ColorSchemeMode.Dark
    else -> ColorSchemeMode.System
}

@Composable
internal fun MiuixControlTheme(content: @Composable () -> Unit) {
    val mode = currentMiuixColorSchemeMode()
    MiuixTheme(controller = remember(mode) { ThemeController(mode) }) {
        content()
    }
}
