package com.zhenl.launcher.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import com.zhenl.launcher.iconpack.DefaultPack
import com.zhenl.launcher.iconpack.IconPackManager
import com.zhenl.launcher.preference.IconPackFragment

/**
 * Created by lin on 2020/10/23.
 */
class IconPackPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {

    private val ipm = IconPackManager.getInstance(context)
    private val packList = ipm.packList

    private val onChangeListener = ::updatePreview

    init {
        layoutResource = R.layout.pref_with_preview_icon
        fragment = IconPackFragment::class.java.name
    }

    override fun onAttached() {
        super.onAttached()

        ipm.addListener(onChangeListener)
    }

    override fun onDetached() {
        super.onDetached()

        ipm.removeListener(onChangeListener)
    }

    private fun updatePreview() {
        try {
            summary = if (packList.currentPack() is DefaultPack) {
                packList.currentPack().displayName
            } else {
                packList.appliedPacks
                        .filter { it !is DefaultPack }
                        .joinToString(", ") { it.displayName }
            }
            icon = packList.currentPack().displayIcon
        } catch(ignored: IllegalStateException) {
            //TODO: Fix updating pref when scrolled down
        }
    }
}