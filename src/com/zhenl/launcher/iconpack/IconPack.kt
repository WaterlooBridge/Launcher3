package com.zhenl.launcher.iconpack

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfo
import com.android.launcher3.compat.AlphabeticIndexCompat
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.Executors
import com.android.launcher3.util.LooperExecutor
import java.util.concurrent.Semaphore

/**
 * Created by lin on 2020/10/13.
 */
abstract class IconPack(val context: Context, val packPackageName: String) {
    private var waiter: Semaphore? = Semaphore(0)
    private val indexCompat by lazy { AlphabeticIndexCompat(context) }
    private val loadCompleteListeners = ArrayList<(IconPack) -> Unit>()

    fun executeLoadPack() {
        Executors.ICON_PACK_EXECUTOR.execute {
            loadPack()
            waiter?.release()
            loadCompleteListeners.forEach { it.invoke(this) }
            loadCompleteListeners.clear()
        }
    }

    fun addOnLoadCompleteListener(listener: (IconPack) -> Unit) {
        if (waiter != null) loadCompleteListeners.add(listener)
        else listener.invoke(this)
    }

    @Synchronized
    fun ensureInitialLoadComplete() {
        waiter?.run {
            acquireUninterruptibly()
            release()
            waiter = null
        }
    }

    val displayIcon get() = packInfo.displayIcon
    val displayName get() = packInfo.displayName

    abstract val packInfo: IconPackList.PackInfo

    abstract fun onDateChanged()

    abstract fun loadPack()

    abstract fun getEntryForComponent(key: ComponentKey): Entry?

    open fun getMaskEntryForComponent(key: ComponentKey): Entry? = null

    open fun getIcon(entry: IconPackManager.CustomIconEntry, iconDpi: Int): Drawable? {
        return null
    }

    abstract fun getIcon(launcherActivityInfo: LauncherActivityInfo,
                         iconDpi: Int, flattenDrawable: Boolean,
                         customIconEntry: IconPackManager.CustomIconEntry?,
                         iconProvider: LauncherIconProvider?): Drawable?

    abstract fun getIcon(shortcutInfo: ShortcutInfoCompat, iconDpi: Int): Drawable?

    abstract fun newIcon(icon: Bitmap, itemInfo: ItemInfo,
                         customIconEntry: IconPackManager.CustomIconEntry?,
                         drawableFactory: LauncherDrawableFactory): FastBitmapDrawable?

    open fun getAllIcons(callback: (List<PackEntry>) -> Unit, cancel: () -> Boolean, filter: (item: String) -> Boolean = { _ -> true }) {
        ensureInitialLoadComplete()
        callback(categorize(filterDuplicates(entries)).filter { if (it is Entry) filter(it.identifierName) else true })
    }

    abstract fun supportsMasking(): Boolean

    private fun filterDuplicates(entries: List<Entry>): List<Entry> {
        var previous = ""
        val filtered = ArrayList<Entry>()
        entries.sortedBy { it.identifierName }.forEach {
            if (it.identifierName != previous) {
                previous = it.identifierName
                filtered.add(it)
            }
        }
        return filtered
    }

    private fun categorize(entries: List<Entry>): List<PackEntry> {
        val packEntries = ArrayList<PackEntry>()
        var previousSection = ""
        entries.sortedBy { it.displayName.toLowerCase() }.forEach {
            val currentSection = indexCompat.computeSectionName(it.displayName)
            if (currentSection != previousSection) {
                previousSection = currentSection
                packEntries.add(CategoryTitle(currentSection))
            }
            packEntries.add(it)
        }
        return packEntries
    }

    abstract val entries: List<Entry>

    open class PackEntry

    class CategoryTitle(val title: String) : PackEntry()

    abstract class Entry : PackEntry() {

        abstract val displayName: String
        abstract val identifierName: String
        val drawable get() = drawableForDensity(0)
        abstract val isAvailable: Boolean

        abstract fun drawableForDensity(density: Int): Drawable

        abstract fun toCustomEntry(): IconPackManager.CustomIconEntry
    }
}