package su.android.ui.appinfo

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.Formatter
import androidx.databinding.Bindable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import su.android.BR
import su.android.R
import su.android.arch.AsyncLoadViewModel
import su.android.core.AppContext
import su.android.core.ktx.getLabel
import su.android.databinding.bindExtra
import su.android.databinding.diffList
import su.android.databinding.set
import java.io.File
import java.security.MessageDigest
import java.text.DateFormat
import java.util.Date

class AppDetailViewModel : AsyncLoadViewModel() {

    private var packageName: String = ""

    val items = diffList<AppDetailRvItem>()

    val extraBindings = bindExtra { }

    @get:Bindable
    var loading = true
        private set(value) = set(value, field, { field = it }, BR.loading)

    fun setPackageName(name: String) {
        packageName = name
    }

    override suspend fun doLoadWork() {
        loading = true
        val list = withContext(Dispatchers.IO) { buildItems(AppContext, packageName) }
        withContext(Dispatchers.Default) { items.update(list) }
        loading = false
    }

    private fun buildItems(context: Context, pkg: String): List<AppDetailRvItem> {
        val result = mutableListOf<AppDetailRvItem>()
        val pm = context.packageManager
        val na = context.getString(R.string.not_available)

        val pi = runCatching { pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS) }
            .getOrNull() ?: return result
        val app = pi.applicationInfo ?: return result

        val label = runCatching { app.getLabel(pm) }.getOrDefault(pkg)
        val versionName = pi.versionName.orEmpty()
        val versionCode =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode
            else pi.versionCode.toLong()
        val icon = runCatching { app.loadIcon(pm) }.getOrDefault(pm.defaultActivityIcon)

        fun yesNo(value: Boolean) =
            context.getString(if (value) R.string.app_info_yes else R.string.app_info_no)

        result += AppDetailHeaderItem(label, pkg, versionName, icon)

        // Application
        result += AppDetailSectionItem(context.getString(R.string.app_info_section_application))
        result += AppDetailInfoItem(R.drawable.ic_apps, context.getString(R.string.app_info_label_name), label)
        result += AppDetailInfoItem(R.drawable.ic_package, context.getString(R.string.app_info_label_package), pkg)
        result += AppDetailInfoItem(
            R.drawable.ic_version,
            context.getString(R.string.app_info_label_version),
            versionName.ifEmpty { na }
        )
        result += AppDetailInfoItem(
            R.drawable.ic_code,
            context.getString(R.string.app_info_label_version_code),
            versionCode.toString()
        )
        result += AppDetailInfoItem(R.drawable.ic_person, context.getString(R.string.app_info_label_uid), app.uid.toString())
        result += AppDetailInfoItem(
            R.drawable.ic_memory,
            context.getString(R.string.app_info_label_process),
            app.processName ?: pkg
        )
        result += AppDetailInfoItem(
            R.drawable.ic_info,
            context.getString(R.string.app_info_label_category),
            categoryName(context, app)
        )
        result += AppDetailInfoItem(
            R.drawable.ic_check_circle,
            context.getString(R.string.app_info_label_enabled),
            yesNo(app.enabled)
        )
        pm.getLaunchIntentForPackage(pkg)?.component?.className?.let {
            result += AppDetailInfoItem(R.drawable.ic_android, context.getString(R.string.app_info_label_launchable), it)
        }

        // System
        result += AppDetailSectionItem(context.getString(R.string.app_info_section_system))
        result += AppDetailInfoItem(
            R.drawable.ic_shield,
            context.getString(R.string.app_info_label_system_app),
            yesNo(isSystem(app))
        )
        result += AppDetailInfoItem(
            R.drawable.ic_android,
            context.getString(R.string.app_info_label_min_sdk),
            minSdk(app).toString()
        )
        result += AppDetailInfoItem(
            R.drawable.ic_android,
            context.getString(R.string.app_info_label_target_sdk),
            targetSdk(app).toString()
        )
        result += AppDetailInfoItem(R.drawable.ic_info, context.getString(R.string.app_info_label_flags), flags(app.flags))

        // Storage
        result += AppDetailSectionItem(context.getString(R.string.app_info_section_storage))
        result += AppDetailInfoItem(R.drawable.ic_folder, context.getString(R.string.app_info_label_apk_path), app.sourceDir)
        result += AppDetailInfoItem(
            R.drawable.ic_storage,
            context.getString(R.string.app_info_label_apk_size),
            apkSize(context, app.sourceDir, na)
        )
        result += AppDetailInfoItem(
            R.drawable.ic_folder,
            context.getString(R.string.app_info_label_data_dir),
            dataDir(app) ?: na
        )
        result += AppDetailInfoItem(
            R.drawable.ic_folder,
            context.getString(R.string.app_info_label_native_lib),
            app.nativeLibraryDir ?: na
        )
        result += AppDetailInfoItem(
            R.drawable.ic_pin,
            context.getString(R.string.app_info_label_install_location),
            installLocation(context, pi.installLocation)
        )

        // Security
        result += AppDetailSectionItem(context.getString(R.string.app_info_section_security))
        result += AppDetailInfoItem(
            R.drawable.ic_key,
            context.getString(R.string.app_info_label_signature),
            signature(pm, pkg, na)
        )
        result += AppDetailInfoItem(
            R.drawable.ic_lock,
            context.getString(R.string.app_info_label_permissions),
            permissions(context, pi)
        )
        result += AppDetailInfoItem(
            R.drawable.ic_person,
            context.getString(R.string.app_info_label_shared_uid),
            pi.sharedUserId ?: context.getString(R.string.none)
        )

        // Dates
        result += AppDetailSectionItem(context.getString(R.string.app_info_section_dates))
        result += AppDetailInfoItem(
            R.drawable.ic_clock,
            context.getString(R.string.app_info_label_first_install),
            date(pi.firstInstallTime, na)
        )
        result += AppDetailInfoItem(
            R.drawable.ic_update,
            context.getString(R.string.app_info_label_last_update),
            date(pi.lastUpdateTime, na)
        )

        return result
    }

    private fun isSystem(app: ApplicationInfo) =
        app.flags and ApplicationInfo.FLAG_SYSTEM != 0

    private fun minSdk(app: ApplicationInfo) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) app.minSdkVersion else 0

    private fun targetSdk(app: ApplicationInfo) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) app.targetSdkVersion else 0

    private fun dataDir(app: ApplicationInfo): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) app.dataDir else null

    private fun categoryName(context: Context, app: ApplicationInfo): String {
        val category =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) app.category
            else ApplicationInfo.CATEGORY_UNDEFINED
        val res = when (category) {
            ApplicationInfo.CATEGORY_GAME -> R.string.app_info_category_game
            ApplicationInfo.CATEGORY_AUDIO -> R.string.app_info_category_audio
            ApplicationInfo.CATEGORY_VIDEO -> R.string.app_info_category_video
            ApplicationInfo.CATEGORY_IMAGE -> R.string.app_info_category_image
            ApplicationInfo.CATEGORY_SOCIAL -> R.string.app_info_category_social
            ApplicationInfo.CATEGORY_NEWS -> R.string.app_info_category_news
            ApplicationInfo.CATEGORY_MAPS -> R.string.app_info_category_maps
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> R.string.app_info_category_productivity
            ApplicationInfo.CATEGORY_ACCESSIBILITY -> R.string.app_info_category_accessibility
            else -> 0
        }
        return if (res != 0) context.getString(res) else context.getString(R.string.app_info_category_other)
    }

    private fun flags(flags: Int): String {
        val names = ArrayList<String>(10)
        if (flags and ApplicationInfo.FLAG_SYSTEM != 0) names += "SYSTEM"
        if (flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) names += "DEBUGGABLE"
        if (flags and ApplicationInfo.FLAG_HAS_CODE != 0) names += "HAS_CODE"
        if (flags and ApplicationInfo.FLAG_PERSISTENT != 0) names += "PERSISTENT"
        if (flags and ApplicationInfo.FLAG_LARGE_HEAP != 0) names += "LARGE_HEAP"
        if (flags and ApplicationInfo.FLAG_ALLOW_BACKUP != 0) names += "ALLOW_BACKUP"
        if (flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE != 0) names += "EXTERNAL_STORAGE"
        if (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) names += "UPDATED_SYSTEM_APP"
        if (flags and ApplicationInfo.FLAG_INSTALLED != 0) names += "INSTALLED"
        if (flags and ApplicationInfo.FLAG_STOPPED != 0) names += "STOPPED"
        return if (names.isEmpty()) "0x%08X".format(flags) else names.joinToString(", ")
    }

    private fun apkSize(context: Context, sourceDir: String, fallback: String): String =
        runCatching { Formatter.formatFileSize(context, File(sourceDir).length()) }.getOrDefault(fallback)

    private fun installLocation(context: Context, location: Int) = when (location) {
        PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> context.getString(R.string.app_info_install_internal)
        PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> context.getString(R.string.app_info_install_external)
        else -> context.getString(R.string.app_info_install_auto)
    }

    @Suppress("DEPRECATION")
    private fun signature(pm: PackageManager, pkg: String, fallback: String): String = runCatching {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners
        } else {
            pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES).signatures
        }
        val first = signatures?.firstOrNull() ?: return@runCatching fallback
        MessageDigest.getInstance("SHA-256")
            .digest(first.toByteArray())
            .joinToString(":") { "%02X".format(it.toInt() and 0xFF) }
    }.getOrDefault(fallback)

    private fun permissions(context: Context, pi: PackageInfo): String {
        val count = pi.requestedPermissions?.size ?: 0
        return if (count == 0) context.getString(R.string.none)
        else context.getString(R.string.app_info_permissions_count, count)
    }

    private fun date(millis: Long, fallback: String) =
        if (millis <= 0) fallback
        else DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(millis))
}
