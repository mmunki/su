package su.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import su.android.R
import su.android.arch.BaseFragment
import su.android.arch.viewModel
import su.android.core.download.DownloadEngine
import su.android.databinding.FragmentHomeBinding
import su.android.R as CoreR

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override val layoutRes = R.layout.fragment_home
    override val viewModel by viewModel<HomeViewModel>()

    override fun onStart() {
        super.onStart()
        activity?.setTitle(CoreR.string.section_home)
        DownloadEngine.observeProgress(this, viewModel::onProgressUpdate)
    }

    private fun checkTitle(text: TextView, icon: ImageView) {
        text.post {
            if (text.layout?.getEllipsisCount(0) != 0) {
                with (icon) {
                    layoutParams.width = 0
                    layoutParams.height = 0
                    requestLayout()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        with(binding.homeSystemWrapper) {
            checkTitle(homeSystemTitle, homeSystemIcon)
        }
        with(binding.homeManagerWrapper) {
            checkTitle(homeManagerTitle, homeManagerIcon)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.stateManagerProgress = 0
    }
}
