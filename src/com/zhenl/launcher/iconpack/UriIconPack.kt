package com.zhenl.launcher.iconpack

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import androidx.core.content.pm.ShortcutInfoCompat
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.ItemInfo
import com.android.launcher3.Utilities
import com.android.launcher3.util.ComponentKey
import com.zhenl.launcher.adaptive.AdaptiveIconGenerator
import java.io.FileDescriptor

/**
 * Created by lin on 2020/10/13.
 */
class UriIconPack(context: Context) : IconPack(context, "launcherUriPack") {

    override val packInfo = IconPackList.DefaultPackInfo(context)
    override val entries = listOf<Entry>()

    private val entryCache = mutableMapOf<Pair<String, Boolean>, UriEntry>()

    override fun loadPack() {

    }

    override fun onDateChanged() {

    }

    override fun getEntryForComponent(key: ComponentKey): Entry? {
        throw NotImplementedError()
    }

    private fun getUriEntry(entry: IconPackManager.CustomIconEntry?): UriEntry? {
        if (entry == null) return null
        if (TextUtils.isEmpty(entry.icon)) return null
        val key = entry.icon!! to (entry.arg == "true")
        val uriEntry = entryCache.getOrPut(key) { UriEntry.fromSpec(context, entry.icon, entry.arg) }
        if (!uriEntry.isAvailable) return null
        return uriEntry
    }

    override fun getIcon(entry: IconPackManager.CustomIconEntry, iconDpi: Int): Drawable? {
        return getUriEntry(entry)?.drawable ?: super.getIcon(entry, iconDpi)
    }

    override fun getIcon(shortcutInfo: ShortcutInfoCompat, iconDpi: Int): Drawable? {
        throw NotImplementedError()
    }

    override fun getIcon(launcherActivityInfo: LauncherActivityInfo, iconDpi: Int,
                         flattenDrawable: Boolean, customIconEntry: IconPackManager.CustomIconEntry?,
                         iconProvider: LauncherIconProvider?): Drawable? {
        val entry = getUriEntry(customIconEntry)
        val icon = entry?.drawable
        if (icon != null) {
            return if (Utilities.ATLEAST_OREO && entry.adaptive) {
                AdaptiveIconGenerator(context, icon).result
            } else icon
        }
        return null
    }

    override fun newIcon(icon: Bitmap, itemInfo: ItemInfo,
                         customIconEntry: IconPackManager.CustomIconEntry?,
                         drawableFactory: LauncherDrawableFactory): FastBitmapDrawable? {
        return FastBitmapDrawable(icon)
    }

    override fun supportsMasking() = false

    class UriEntry(private val context: Context, val uri: Uri, var adaptive: Boolean) : Entry() {

        override val identifierName = uri.toString()
        override val displayName = identifierName

        val bitmap by lazy { loadBitmap() }

        override fun drawableForDensity(density: Int): Drawable {
            return BitmapDrawable(context.resources, bitmap)
        }

        override val isAvailable by lazy {
            try {
                bitmap
                true
            } catch (ignored: Throwable) {
                false
            }
        }

        private fun loadBitmap(): Bitmap {
            val parcelFileDescriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")!!
            val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        }

        override fun toCustomEntry(): IconPackManager.CustomIconEntry {
            return IconPackManager.CustomIconEntry("lawnchairUriPack", "$uri", "$adaptive")
        }

        companion object {

            fun fromSpec(context: Context, spec: String?, arg: String?): UriEntry {
                return UriEntry(context, Uri.parse(spec), arg == "true")
            }
        }
    }
}