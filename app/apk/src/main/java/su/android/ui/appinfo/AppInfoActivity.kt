package su.android.ui.appinfo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import su.android.R
import su.android.arch.UIActivity
import su.android.arch.viewModel
import su.android.databinding.ActivityAppInfoBinding
import su.android.ui.theme.Theme

class AppInfoActivity : UIActivity<ActivityAppInfoBinding>() {

    override val layoutRes = R.layout.activity_app_info
    override val viewModel by viewModel<AppInfoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)
        super.onCreate(savedInstanceState)
        setContentView()

        setSupportActionBar(binding.appInfoToolbar)
        supportActionBar?.title = getString(R.string.app_info_title)
        binding.appInfoToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.appInfoToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app_info, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val showSystem = menu.findItem(R.id.action_show_system)
        val showOS = menu.findItem(R.id.action_show_OS)
        if (showSystem != null && showOS != null) {
            showSystem.isChecked = viewModel.isShowSystem
            showOS.isChecked = viewModel.isShowOS
            showOS.isEnabled = showSystem.isChecked
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
            R.id.action_show_system -> {
                val check = !item.isChecked
                viewModel.isShowSystem = check
                item.isChecked = check
                invalidateOptionsMenu()
                return true
            }
            R.id.action_show_OS -> {
                val check = !item.isChecked
                viewModel.isShowOS = check
                item.isChecked = check
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun openApp(view: View, item: AppInfoRvItem) {
        startActivity(AppDetailActivity.intent(this, item.packageName))
    }
}
