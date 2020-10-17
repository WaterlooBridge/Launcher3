package com.zhenl.launcher.iconpack

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import com.android.launcher3.FastBitmapDrawable
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import kotlin.math.abs

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

        var adaptiveBackground: Drawable? = null
        // Some random magic to get an acceptable resolution
        var size = LauncherAppState.getIDP(context).iconBitmapSize
        if (Utilities.ATLEAST_OREO && iconBack?.drawableId != 0 && iconBack?.drawable is AdaptiveIconCompat) {
            if (onlyMaskLegacy && baseIcon is AdaptiveIconCompat)
                return baseIcon
            size += (size * AdaptiveIconCompat.getExtraInsetFraction()).toInt()
        }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw the app icon
        val iconBitmapSize = LauncherAppState.getIDP(context).iconBitmapSize
        val bb = baseIcon.toBitmap(iconBitmapSize, iconBitmapSize)
        matrix.postTranslate((size - bb.width) / 2f, (size - bb.height) / 2f)
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
        if (Utilities.ATLEAST_OREO && adaptiveBackground != null)
            return AdaptiveIconCompat(adaptiveBackground, FastBitmapDrawable(bitmap))
        return FastBitmapDrawable(bitmap)
    }

    private fun <T> getFromList(list: List<T>, key: Any?): T? {
        if (list.isEmpty()) return null
        return list[abs(key.hashCode()) % list.size]
    }
}