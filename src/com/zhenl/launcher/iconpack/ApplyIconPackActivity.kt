package com.zhenl.launcher.iconpack

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import com.zhenl.launcher.launcherPrefs

/**
 * Created by lin on 2020/10/13.
 */
class ApplyIconPackActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = launcherPrefs
        intent.getStringExtra("packageName")?.let {
            prefs.iconPacks.remove(it)
            prefs.iconPacks.add(0, it)
            val packName = IconPackManager.getInstance(this).packList.currentPack().displayName
            Toast.makeText(this, "“$packName” applied.", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}