package com.zhenl.launcher.override

import android.content.Context
import com.android.launcher3.AppInfo
import com.android.launcher3.ItemInfo
import com.zhenl.launcher.iconpack.IconPackManager

/**
 * Created by lin on 2020/10/13.
 */
abstract class CustomInfoProvider<in T : ItemInfo>(val context: Context) {

    abstract fun getTitle(info: T): String

    abstract fun getDefaultTitle(info: T): String

    abstract fun getCustomTitle(info: T): String?

    abstract fun setTitle(info: T, title: String?)

    open fun supportsIcon() = true

    abstract fun setIcon(info: T, entry: IconPackManager.CustomIconEntry?)

    abstract fun getIcon(info: T): IconPackManager.CustomIconEntry?

    open fun supportsSwipeUp(info: T) = false

    open fun setSwipeUpAction(info: T, action: String?) {
    }

    open fun getSwipeUpAction(info: T): String? = null

    open fun supportsBadgeVisible(info: T) = false

    open fun setBadgeVisible(info: T, visible: Boolean) {
    }

    open fun getBadgeVisible(info: T): Boolean = false

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T : ItemInfo> forItem(context: Context, info: ItemInfo?): CustomInfoProvider<T>? {
            return when (info) {
                is AppInfo -> AppInfoProvider.getInstance(context)
                else -> null
            } as CustomInfoProvider<T>?
        }
    }
}