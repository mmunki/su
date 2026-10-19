package su.android.ui.appinfo

import su.android.arch.ActivityExecutor
import su.android.arch.UIActivity
import su.android.arch.ViewEvent

class AppDetailEvent(private val packageName: String) : ViewEvent(), ActivityExecutor {

    override fun invoke(activity: UIActivity<*>) {
        activity.startActivity(AppDetailActivity.intent(activity, packageName))
    }
}
