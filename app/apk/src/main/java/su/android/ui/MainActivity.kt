package su.android.ui

import android.Manifest
import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import su.android.MainDirections
import su.android.R
import su.android.arch.BaseViewModel
import su.android.arch.NavigationActivity
import su.android.arch.startAnimations
import su.android.arch.viewModel
import su.android.core.Config
import su.android.core.Const
import su.android.core.Info
import su.android.core.base.SplashController
import su.android.core.base.SplashScreenHost
import su.android.core.isRunningAsStub
import su.android.core.ktx.toast
import su.android.core.model.module.LocalModule
import su.android.core.tasks.AppMigration
import su.android.databinding.ActivityMainBinding
import su.android.ui.theme.Theme
import su.android.view.MaterialDialog
import su.android.view.Shortcuts
import kotlinx.coroutines.launch
import java.io.File
import su.android.R as CoreR

class MainViewModel : BaseViewModel()

class MainActivity : NavigationActivity<ActivityMainBinding>(), SplashScreenHost {

    override val layoutRes = R.layout.activity_main
    override val viewModel by viewModel<MainViewModel>()
    override val navHostId: Int = R.id.main_nav_host
    override val splashController = SplashController(this)
    override val snackbarView: View
        get() {
            val fragmentOverride = currentFragment?.snackbarView
            return fragmentOverride ?: super.snackbarView
        }
    override val snackbarAnchorView: View?
        get() {
            val fragmentAnchor = currentFragment?.snackbarAnchorView
            return when {
                fragmentAnchor?.isVisible == true -> fragmentAnchor
                else -> null
            }
        }

    /** One entry of the bottom navigation bar. */
    private class NavToggle(
        val id: Int,
        val view: View
    )

    private var navToggles: List<NavToggle> = emptyList()

    private var isRootFragment = true

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)
        splashController.preOnCreate()
        super.onCreate(savedInstanceState)
        splashController.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        splashController.onResume()
    }

    @SuppressLint("InlinedApi")
    override fun onCreateUi(savedInstanceState: Bundle?) {
        setContentView()
        showUnsupportedMessage()
        askForHomeShortcut()

        if (Config.checkUpdate) {
            withPermission(Manifest.permission.POST_NOTIFICATIONS) {
                Config.checkUpdate = it
            }
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        navigation.addOnDestinationChangedListener { _, destination, _ ->
            isRootFragment = when (destination.id) {
                R.id.homeFragment,
                R.id.modulesFragment,
                R.id.superuserFragment,
                R.id.logFragment,
                R.id.settingsFragment -> true
                else -> false
            }

            setDisplayHomeAsUpEnabled(!isRootFragment)
            requestNavigationHidden(!isRootFragment, savedInstanceState == null)

            navToggles.forEach { toggle ->
                toggle.view.isSelected = toggle.id == destination.id
            }
        }

        setSupportActionBar(binding.mainToolbar)

        navToggles = listOf(
            NavToggle(R.id.homeFragment, binding.mainNavHome),
            NavToggle(R.id.superuserFragment, binding.mainNavSuperuser),
            NavToggle(R.id.modulesFragment, binding.mainNavModules),
            NavToggle(R.id.settingsFragment, binding.mainNavSettings)
        )
        navToggles.forEach { toggle ->
            toggle.view.setOnClickListener {
                if (!toggle.view.isSelected) {
                    getScreen(toggle.id)?.navigate()
                }
            }
        }
        binding.mainNavSuperuser.isEnabled = Info.showSuperUser
        binding.mainNavModules.isEnabled = Info.env.isActive && LocalModule.loaded()

        val section =
            if (intent.action == Intent.ACTION_APPLICATION_PREFERENCES)
                Const.Nav.SETTINGS
            else
                intent.getStringExtra(Const.Key.OPEN_SECTION)

        getScreen(section)?.navigate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun setDisplayHomeAsUpEnabled(isEnabled: Boolean) {
        binding.mainToolbar.startAnimations()
        when {
            isEnabled -> binding.mainToolbar.setNavigationIcon(R.drawable.ic_back)
            else -> binding.mainToolbar.navigationIcon = null
        }
    }

    internal fun requestNavigationHidden(hide: Boolean = true, requiresAnimation: Boolean = true) {
        val navigation = binding.mainNavigation
        if (requiresAnimation) {
            navigation.isVisible = true
            navigation.translationY =
                if (hide) navigation.height.toFloat() + (20 * resources.displayMetrics.density) else 0f
            navigation.animate()
                .translationY(if (hide) navigation.height.toFloat() + (20 * resources.displayMetrics.density) else 0f)
                .setDuration(200)
                .start()
        } else {
            navigation.translationY = 0f
            navigation.isGone = hide
        }
    }

    fun invalidateToolbar() {
        binding.mainToolbar.invalidate()
    }

    private fun getScreen(name: String?): NavDirections? {
        return when (name) {
            Const.Nav.SUPERUSER -> MainDirections.actionSuperuserFragment()
            Const.Nav.MODULES -> MainDirections.actionModuleFragment()
            Const.Nav.SETTINGS -> MainDirections.actionGlobalSettingsFragment()
            else -> null
        }
    }

    private fun getScreen(id: Int): NavDirections? {
        return when (id) {
            R.id.homeFragment -> MainDirections.actionHomeFragment()
            R.id.modulesFragment -> MainDirections.actionModuleFragment()
            R.id.superuserFragment -> MainDirections.actionSuperuserFragment()
            R.id.logFragment -> MainDirections.actionLogFragment()
            R.id.denyFragment -> MainDirections.actionGlobalDenyFragment()
            R.id.settingsFragment -> MainDirections.actionGlobalSettingsFragment()
            else -> null
        }
    }

    @SuppressLint("InlinedApi")
    override fun showInvalidStateMessage(): Unit = runOnUiThread {
        MaterialDialog(this).apply {
            setTitle(CoreR.string.unsupport_nonroot_stub_title)
            setMessage(CoreR.string.unsupport_nonroot_stub_msg)
            setButton(MaterialDialog.ButtonType.POSITIVE) {
                text = CoreR.string.install
                onClick {
                    withPermission(REQUEST_INSTALL_PACKAGES) {
                        if (!it) {
                            toast(CoreR.string.install_unknown_denied, Toast.LENGTH_SHORT)
                            showInvalidStateMessage()
                        } else {
                            lifecycleScope.launch {
                                AppMigration.restore(this@MainActivity)
                            }
                        }
                    }
                }
            }
            setCancelable(false)
            show()
        }
    }

    private fun showUnsupportedMessage() {
        if (Info.env.isUnsupported) {
            MaterialDialog(this).apply {
                setTitle(CoreR.string.unsupported_root_title)
                setMessage(CoreR.string.unsupported_root_msg, Const.Version.MIN_VERSION)
                setButton(MaterialDialog.ButtonType.POSITIVE) { text = android.R.string.ok }
                setCancelable(false)
            }.show()
        }

        if (!Info.isEmulator && Info.env.isActive && System.getenv("PATH")
                ?.split(':')
                ?.filterNot { File("$it/magisk").exists() }
                ?.any { File("$it/su").exists() } == true) {
            MaterialDialog(this).apply {
                setTitle(CoreR.string.unsupport_general_title)
                setMessage(CoreR.string.unsupport_other_su_msg)
                setButton(MaterialDialog.ButtonType.POSITIVE) { text = android.R.string.ok }
                setCancelable(false)
            }.show()
        }

        if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            MaterialDialog(this).apply {
                setTitle(CoreR.string.unsupport_general_title)
                setMessage(CoreR.string.unsupport_system_app_msg)
                setButton(MaterialDialog.ButtonType.POSITIVE) { text = android.R.string.ok }
                setCancelable(false)
            }.show()
        }

        if (applicationInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE != 0) {
            MaterialDialog(this).apply {
                setTitle(CoreR.string.unsupport_general_title)
                setMessage(CoreR.string.unsupport_external_storage_msg)
                setButton(MaterialDialog.ButtonType.POSITIVE) { text = android.R.string.ok }
                setCancelable(false)
            }.show()
        }
    }

    private fun askForHomeShortcut() {
        if (isRunningAsStub && !Config.askedHome &&
            ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
            Config.askedHome = true
            MaterialDialog(this).apply {
                setTitle(CoreR.string.add_shortcut_title)
                setMessage(CoreR.string.add_shortcut_msg)
                setButton(MaterialDialog.ButtonType.NEGATIVE) {
                    text = android.R.string.cancel
                }
                setButton(MaterialDialog.ButtonType.POSITIVE) {
                    text = android.R.string.ok
                    onClick {
                        Shortcuts.addHomeIcon(this@MainActivity)
                    }
                }
                setCancelable(true)
            }.show()
        }
    }
}
