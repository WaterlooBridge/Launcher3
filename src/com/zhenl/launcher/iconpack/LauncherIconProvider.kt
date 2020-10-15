package com.zhenl.launcher.iconpack

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import com.android.launcher3.ItemInfo
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.google.android.apps.nexuslauncher.DynamicIconProvider

/**
 * Created by lin on 2020/10/13.
 */
class LauncherIconProvider(context: Context) : DynamicIconProvider(context) {

    private val iconPackManager by lazy { IconPackManager.getInstance(context) }

    override fun getIcon(launcherActivityInfo: LauncherActivityInfo, iconDpi: Int, flattenDrawable: Boolean): Drawable {
        return iconPackManager.getIcon(launcherActivityInfo, iconDpi, flattenDrawable, null, this).assertNotAdaptiveIconDrawable(launcherActivityInfo)
    }

    fun getIcon(launcherActivityInfo: LauncherActivityInfo, itemInfo: ItemInfo, iconDpi: Int, flattenDrawable: Boolean): Drawable {
        return iconPackManager.getIcon(launcherActivityInfo, iconDpi, flattenDrawable, itemInfo, this).assertNotAdaptiveIconDrawable(launcherActivityInfo)
    }

    fun getIcon(shortcutInfo: ShortcutInfoCompat, iconDpi: Int): Drawable? {
        return iconPackManager.getIcon(shortcutInfo, iconDpi).assertNotAdaptiveIconDrawable(shortcutInfo)
    }

    fun getDynamicIcon(launcherActivityInfo: LauncherActivityInfo?, iconDpi: Int, flattenDrawable: Boolean): Drawable {
        return super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable).assertNotAdaptiveIconDrawable(launcherActivityInfo)
    }

    private fun <T> T.assertNotAdaptiveIconDrawable(info: Any?): T {
        if (Utilities.ATLEAST_OREO && this is AdaptiveIconDrawable) {
            error("unwrapped AdaptiveIconDrawable for ${
                if (info is LauncherActivityInfo) info.applicationInfo else info
            }")
        }
        return this
    }

    companion object {

        @JvmStatic
        fun getAdaptiveIconDrawableWrapper(context: Context): AdaptiveIconCompat {
            return AdaptiveIconCompat.wrap(context.getDrawable(
                    R.drawable.adaptive_icon_drawable_wrapper)!!.mutate()) as AdaptiveIconCompat
        }
    }
}