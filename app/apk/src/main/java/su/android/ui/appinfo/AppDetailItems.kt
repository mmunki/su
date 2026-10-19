package su.android.ui.appinfo

import android.graphics.drawable.Drawable
import su.android.R
import su.android.databinding.DiffItem
import su.android.databinding.ObservableRvItem

abstract class AppDetailRvItem : ObservableRvItem(), DiffItem<AppDetailRvItem> {

    abstract val key: String

    open val contentKey: String get() = key

    override fun itemSameAs(other: AppDetailRvItem) = key == other.key

    override fun contentSameAs(other: AppDetailRvItem) = contentKey == other.contentKey
}

class AppDetailHeaderItem(
    val label: String,
    val packageName: String,
    val versionName: String,
    val icon: Drawable
) : AppDetailRvItem() {

    override val layoutRes get() = R.layout.item_app_detail_header

    override val key get() = "header"

    override val contentKey get() = "$label|$versionName"
}

class AppDetailSectionItem(
    val title: String
) : AppDetailRvItem() {

    override val layoutRes get() = R.layout.item_app_detail_section

    override val key get() = "section:$title"
}

class AppDetailActionsItem : AppDetailRvItem() {

    override val layoutRes get() = R.layout.item_app_detail_actions

    override val key get() = "actions"
}

class AppDetailInfoItem(
    val icon: Int,
    val label: String,
    val value: String
) : AppDetailRvItem() {

    override val layoutRes get() = R.layout.item_app_detail_info

    override val key get() = "info:$label"

    override val contentKey get() = "$label=$value"
}
