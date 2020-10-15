package com.zhenl.launcher.iconpack

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.android.launcher3.icons.FixedScaleDrawable

/**
 * Created by lin on 2020/10/13.
 */
class IconMask {
    val hasMask by lazy { validBacks.isNotEmpty() || validMasks.isNotEmpty() || validUpons.isNotEmpty() }
    var onlyMaskLegacy: Boolean = false
    var iconScale = 1f
    val matrix = Matrix()
    val paint = Paint()

    val iconBackEntries = ArrayList<IconPackImpl.Entry>()
    val iconMaskEntries = ArrayList<IconPackImpl.Entry>()
    val iconUponEntries = ArrayList<IconPackImpl.Entry>()

    private val validBacks by lazy { iconBackEntries.filter { it.isAvailable } }
    private val validMasks by lazy { iconMaskEntries.filter { it.isAvailable } }
    private val validUpons by lazy { iconUponEntries.filter { it.isAvailable } }

    fun getIcon(context: Context, baseIcon: Drawable, key: Any?): Drawable {
        val iconBack = getFromList(validBacks, key)
        val iconMask = getFromList(validMasks, key)
        val iconUpon = getFromList(validUpons, key)
        val scale = getScale(iconBack)

        var adaptiveBackground: Drawable? = null
        // Some random magic to get an acceptable resolution
        var size = (LauncherAppState.getIDP(context).iconBitmapSize * (3 - scale)).toInt()
        if (Utilities.ATLEAST_OREO && iconBack?.drawableId != 0 && iconBack?.drawable is AdaptiveIconCompat) {
            size += (size * AdaptiveIconCompat.getExtraInsetFraction()).toInt()
        }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw the app icon
        val iconBitmapSize = LauncherAppState.getIDP(context).iconBitmapSize
        var bb = baseIcon.toBitmap(iconBitmapSize, iconBitmapSize)
        if (!bb.isMutable) bb = bb.copy(bb.config, true)
        matrix.setScale((size * scale) / bb.width, (size * scale) / bb.height)
        matrix.postTranslate((size / 2) * (1 - scale), (size / 2) * (1 - scale))
        canvas.drawBitmap(bb, matrix, paint)
        matrix.reset()

        // Mask the app icon
        if (iconMask != null && iconMask.drawableId != 0) {
            iconMask.drawable.toBitmap(iconBitmapSize, iconBitmapSize).let {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                matrix.setScale(size.toFloat() / it.width, size.toFloat() / it.height)
                canvas.drawBitmap(it, matrix, paint)
                matrix.reset()
            }
            paint.reset()
        }

        // Draw iconBack
        if (iconBack != null && iconBack.drawableId != 0) {
            val drawable = iconBack.drawable
            if (Utilities.ATLEAST_OREO && drawable is AdaptiveIconCompat) {
                adaptiveBackground = drawable.background
            } else {
                drawable.toBitmap().let {
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
                    matrix.setScale(size.toFloat() / it.width, size.toFloat() / it.height)
                    canvas.drawBitmap(it, matrix, paint)
                    matrix.reset()
                }
                paint.reset()
            }
        }

        // Draw iconUpon
        if (iconUpon != null && iconUpon.drawableId != 0) {
            iconUpon.drawable.toBitmap().let {
                matrix.setScale(size.toFloat() / it.width, size.toFloat() / it.height)
                canvas.drawBitmap(it, matrix, paint)
                matrix.reset()
            }
        }
        if (adaptiveBackground != null) {
            if (onlyMaskLegacy && baseIcon is AdaptiveIconCompat) {
                return baseIcon
            }
            if(Utilities.ATLEAST_OREO)
            return AdaptiveIconCompat(adaptiveBackground, FastBitmapDrawable(bitmap))
        }
        return FastBitmapDrawable(bitmap)
    }

    private fun getScale(iconBack: IconPackImpl.Entry?): Float {
        return if (Utilities.ATLEAST_OREO && iconBack?.drawable is AdaptiveIconCompat) {
            iconScale - (1f - FixedScaleDrawable.LEGACY_ICON_SCALE)
        } else {
            iconScale
        }
    }

    private fun <T> getFromList(list: List<T>, key: Any?): T? {
        if (list.isEmpty()) return null
        return list[Math.abs(key.hashCode()) % list.size]
    }
}