package su.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

data class MiuixNavItem(
    val id: Int,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
)

@Composable
fun MiuixShellTopBar(
    title: String,
    backEnabled: Boolean,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    SmallTopAppBar(
        title = title,
        color = Color.Transparent,
        navigationIcon = {
            if (backEnabled) {
                IconButton(onClick = onBack) {
                    Icon(MiuixIcons.Back, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        defaultWindowInsetsPadding = false,
    )
}

@Composable
fun MiuixShellBottomBar(
    items: List<MiuixNavItem>,
    selectedId: Int,
    visible: Boolean,
    onSelect: (Int) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent,
            showDivider = true,
            defaultWindowInsetsPadding = false,
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    selected = item.id == selectedId,
                    onClick = { if (item.enabled) onSelect(item.id) },
                    icon = item.icon,
                    label = item.label,
                    enabled = item.enabled,
                )
            }
        }
    }
}
