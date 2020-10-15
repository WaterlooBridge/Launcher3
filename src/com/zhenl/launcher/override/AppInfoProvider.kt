package com.zhenl.launcher.override

import android.content.Context
import android.content.pm.LauncherActivityInfo
import com.android.launcher3.AppInfo
import com.android.launcher3.LauncherAppState
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.util.ComponentKey
import com.zhenl.launcher.iconpack.IconPackManager
import com.zhenl.launcher.launcherPrefs
import com.zhenl.launcher.useApplicationContext
import com.zhenl.launcher.util.SingletonHolder

/**
 * Created by lin on 2020/10/13.
 */
class AppInfoProvider private constructor(context: Context) : CustomInfoProvider<AppInfo>(context) {

    private val prefs = context.launcherPrefs
    private val launcherApps by lazy { LauncherAppsCompat.getInstance(context) }

    override fun getTitle(info: AppInfo): String {
        return prefs.customAppName[info.toComponentKey()] ?: info.title.toString()
    }

    override fun getDefaultTitle(info: AppInfo): String {
        val app = getLauncherActivityInfo(info)
        return app?.label?.toString() ?: "" // TODO: can this really be null?
    }

    override fun getCustomTitle(info: AppInfo): String? {
        return prefs.customAppName[ComponentKey(info.componentName, info.user)]
    }

    fun getTitle(app: LauncherActivityInfo): CharSequence {
        return prefs.customAppName[getComponentKey(app)] ?: app.label
    }

    override fun setTitle(info: AppInfo, title: String?) {
        setTitle(info.toComponentKey(), title)
    }

    fun setTitle(key: ComponentKey, title: String?) {
        prefs.customAppName[key] = title
        LauncherAppState.getInstance(context).iconCache.updateIconsForPkg(key.componentName.packageName, key.user)
    }

    override fun setIcon(info: AppInfo, entry: IconPackManager.CustomIconEntry?) {
        setIcon(info.toComponentKey(), entry)
    }

    fun setIcon(key: ComponentKey, entry: IconPackManager.CustomIconEntry?) {
        prefs.customAppIcon[key] = entry
        LauncherAppState.getInstance(context).iconCache.updateIconsForPkg(key.componentName.packageName, key.user)
    }

    private fun getLauncherActivityInfo(info: AppInfo): LauncherActivityInfo? {
        return launcherApps.resolveActivity(info.getIntent(), info.user)
    }

    fun getCustomIconEntry(app: LauncherActivityInfo): IconPackManager.CustomIconEntry? {
        return getCustomIconEntry(getComponentKey(app))
    }

    fun getCustomIconEntry(key: ComponentKey): IconPackManager.CustomIconEntry? {
        return prefs.customAppIcon[key]
    }

    override fun getIcon(info: AppInfo): IconPackManager.CustomIconEntry? {
        return getCustomIconEntry(info.toComponentKey())
    }

    private fun getComponentKey(app: LauncherActivityInfo) = ComponentKey(app.componentName, app.user)

    companion object : SingletonHolder<AppInfoProvider, Context>(useApplicationContext(::AppInfoProvider))

}