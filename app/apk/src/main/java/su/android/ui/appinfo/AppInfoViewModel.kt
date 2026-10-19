package su.android.ui.appinfo

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.withContext
import su.android.BR
import su.android.arch.AsyncLoadViewModel
import su.android.core.AppContext
import su.android.core.ktx.concurrentMap
import su.android.databinding.bindExtra
import su.android.databinding.filterList
import su.android.databinding.set

class AppInfoViewModel : AsyncLoadViewModel() {

    var isShowSystem = false
        set(value) {
            field = value
            doQuery(query)
        }

    var isShowOS = false
        set(value) {
            field = value
            doQuery(query)
        }

    var query = ""
        set(value) {
            field = value
            doQuery(value)
        }

    val items = filterList<AppInfoRvItem>(viewModelScope)

    val extraBindings = bindExtra {
        it.put(BR.viewModel, this)
    }

    @get:Bindable
    var loading = true
        private set(value) = set(value, field, { field = it }, BR.loading)

    override suspend fun doLoadWork() {
        loading = true
        val apps = withContext(Dispatchers.Default) {
            val pm = AppContext.packageManager
            val packages = pm.getInstalledPackages(0)
            packages.asFlow()
                .filter { it.applicationInfo != null }
                .concurrentMap { AppInfoRvItem(it, pm) }
                .toCollection(ArrayList(packages.size))
                .also { it.sort() }
        }
        items.set(apps)
        doQuery(query)
    }

    private fun doQuery(s: String) {
        items.filter {
            fun filterSystem() = isShowSystem || !it.isSystemApp

            fun filterOS() = (isShowSystem && isShowOS) || it.isApp

            fun filterQuery() = s.isEmpty() ||
                it.label.contains(s, true) ||
                it.packageName.contains(s, true) ||
                it.versionName.contains(s, true)

            filterSystem() && filterOS() && filterQuery()
        }
        loading = false
    }
}
