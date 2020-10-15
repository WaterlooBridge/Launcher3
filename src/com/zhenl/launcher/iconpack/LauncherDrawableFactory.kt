package com.zhenl.launcher.iconpack

import android.content.Context
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfoWithIcon
import com.google.android.apps.nexuslauncher.DynamicDrawableFactory
import com.google.android.apps.nexuslauncher.clock.CustomClock

/**
 * Created by lin on 2020/10/13.
 */
class LauncherDrawableFactory(context: Context) : DynamicDrawableFactory(context) {

    private val iconPackManager = IconPackManager.getInstance(context)
    val customClockDrawer by lazy { CustomClock(context) }

    override fun newIcon(context: Context?, info: ItemInfoWithIcon): FastBitmapDrawable {
        return iconPackManager.newIcon(info.iconBitmap,
                info, this).also { it.setIsDisabled(info.isDisabled) }
    }
}