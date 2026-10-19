package su.android.ui.appinfo

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.os.ProcessCompat
import su.android.R
import su.android.core.ktx.getLabel
import su.android.databinding.DiffItem
import su.android.databinding.ObservableRvItem
import java.util.Locale

class AppInfoRvItem(
    val info: PackageInfo,
    private val pm: PackageManager
) : ObservableRvItem(), DiffItem<AppInfoRvItem>, Comparable<AppInfoRvItem> {

    override val layoutRes get() = R.layout.item_app_info

    private val app: ApplicationInfo get() = info.applicationInfo

    val label: String = runCatching { app.getLabel(pm) }.getOrDefault(info.packageName)
    val packageName: String get() = info.packageName
    val versionName: String = info.versionName.orEmpty()
    val icon: Drawable = runCatching { app.loadIcon(pm) }.getOrDefault(pm.defaultActivityIcon)

    val isSystemApp get() = app.flags and ApplicationInfo.FLAG_SYSTEM != 0

    val isApp get() = ProcessCompat.isApplicationUid(app.uid)

    override fun itemSameAs(other: AppInfoRvItem) = packageName == other.packageName

    override fun contentSameAs(other: AppInfoRvItem) = versionName == other.versionName

    override fun compareTo(other: AppInfoRvItem) = comparator.compare(this, other)

    companion object {
        private val comparator = compareBy<AppInfoRvItem>(
            { it.label.lowercase(Locale.ROOT) },
            { it.packageName }
        )
    }
}
