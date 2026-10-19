package su.android.ui.appinfo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import su.android.R
import su.android.arch.UIActivity
import su.android.arch.viewModel
import su.android.databinding.ActivityAppDetailBinding
import su.android.ui.theme.Theme

class AppDetailActivity : UIActivity<ActivityAppDetailBinding>() {

    override val layoutRes = R.layout.activity_app_detail
    override val viewModel by viewModel<AppDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Theme.selected.themeRes)
        super.onCreate(savedInstanceState)

        viewModel.setPackageName(intent.getStringExtra(EXTRA_PACKAGE).orEmpty())

        setContentView()

        setSupportActionBar(binding.appDetailToolbar)
        supportActionBar?.title = getString(R.string.app_info_detail_title)
        binding.appDetailToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.appDetailToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val EXTRA_PACKAGE = "su.android.extra.APP_INFO_PACKAGE"

        fun intent(context: Context, packageName: String): Intent =
            Intent(context, AppDetailActivity::class.java)
                .putExtra(EXTRA_PACKAGE, packageName)
    }
}
