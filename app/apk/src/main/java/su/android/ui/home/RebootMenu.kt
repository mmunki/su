package su.android.ui.home

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import su.android.R
import su.android.core.Config
import su.android.core.Const
import su.android.core.ktx.reboot as systemReboot
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.window.WindowListPopup

data class RebootMenuItem(
    val id: Int,
    @StringRes val title: Int,
    val selected: Boolean = false,
    val visible: Boolean = true,
)

/**
 * The reboot menu rendered with the Miuix list popup instead of the platform
 * [android.widget.PopupMenu]. The platform menu kept its own dark Holo window
 * background, which is why it looked different from the rest of the app.
 */
@Composable
fun MiuixRebootMenu(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val items = remember(show) { buildItems(context) }

    WindowListPopup(
        show = show,
        alignment = PopupPositionProvider.Align.End,
        enableWindowDim = false,
        onDismissRequest = onDismiss,
    ) {
        ListPopupColumn {
            items.forEachIndexed { index, item ->
                DropdownImpl(
                    item = DropdownItem(
                        text = context.getString(item.title),
                        selected = item.selected,
                    ),
                    optionSize = items.size,
                    isSelected = item.selected,
                    index = index,
                    onSelectedIndexChange = {
                        perform(item.id)
                        onDismiss()
                    },
                )
            }
        }
    }
}

private fun buildItems(context: Context): List<RebootMenuItem> {
    val userspaceSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
        context.getSystemService(PowerManager::class.java)?.isRebootingUserspaceSupported == true
    val safeModeSupported = Const.Version.atLeast_28_0()

    return listOf(
        RebootMenuItem(R.id.action_reboot_normal, R.string.reboot),
        RebootMenuItem(R.id.action_reboot_userspace, R.string.reboot_userspace, visible = userspaceSupported),
        RebootMenuItem(R.id.action_reboot_recovery, R.string.reboot_recovery),
        RebootMenuItem(R.id.action_reboot_bootloader, R.string.reboot_bootloader),
        RebootMenuItem(R.id.action_reboot_download, R.string.reboot_download),
        RebootMenuItem(R.id.action_reboot_edl, R.string.reboot_edl),
        RebootMenuItem(
            id = R.id.action_reboot_safe_mode,
            title = R.string.reboot_safe_mode,
            selected = Config.bootloop >= 2,
            visible = safeModeSupported,
        ),
    ).filter { it.visible }
}

fun perform(itemId: Int) {
    when (itemId) {
        R.id.action_reboot_normal -> systemReboot()
        R.id.action_reboot_userspace -> systemReboot("userspace")
        R.id.action_reboot_bootloader -> systemReboot("bootloader")
        R.id.action_reboot_download -> systemReboot("download")
        R.id.action_reboot_edl -> systemReboot("edl")
        R.id.action_reboot_recovery -> systemReboot("recovery")
        R.id.action_reboot_safe_mode -> {
            Config.bootloop = if (Config.bootloop >= 2) 0 else 2
        }
    }
}
