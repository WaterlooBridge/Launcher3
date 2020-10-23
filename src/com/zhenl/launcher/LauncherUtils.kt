package com.zhenl.launcher

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.UserHandle
import android.text.TextUtils
import android.util.Property
import androidx.annotation.ColorInt
import androidx.dynamicanimation.animation.FloatPropertyCompat
import com.android.launcher3.Utilities
import com.android.launcher3.compat.LauncherAppsCompat
import com.android.launcher3.compat.UserManagerCompat
import com.android.launcher3.icons.ColorExtractor.posterize
import com.android.launcher3.util.ComponentKey
import org.xmlpull.v1.XmlPullParser
import kotlin.reflect.KMutableProperty0


/**
 * Created by lin on 2020/10/13.
 */

val Context.launcherPrefs get() = LauncherPreferences.getInstance(this)

@ColorInt
fun Context.getColorEngineAccent(): Int {
    return getColorAccent()
}

@ColorInt
fun Context.getColorAccent(): Int {
    return getColorAttr(android.R.attr.colorAccent)
}

@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

fun ComponentKey.getLauncherActivityInfo(context: Context): LauncherActivityInfo? {
    return LauncherAppsCompat.getInstance(context).getActivityList(componentName.packageName, user)
            .firstOrNull { it.componentName == componentName }
}

class KFloatPropertyCompat(private val property: KMutableProperty0<Float>, name: String) : FloatPropertyCompat<Any>(name) {

    override fun getValue(`object`: Any) = property.get()

    override fun setValue(`object`: Any, value: Float) {
        property.set(value)
    }
}

class KFloatProperty(private val property: KMutableProperty0<Float>, name: String) : Property<Any, Float>(Float::class.java, name) {

    override fun get(`object`: Any) = property.get()

    override fun set(`object`: Any, value: Float) {
        property.set(value)
    }
}

fun String.asNonEmpty(): String? {
    if (TextUtils.isEmpty(this)) return null
    return this
}

fun String.toTitleCase(): String = splitToSequence(" ").map { it.capitalize() }.joinToString(" ")

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
    return { it -> creator(it.applicationContext) }
}

operator fun XmlPullParser.get(index: Int): String? = getAttributeValue(index)
operator fun XmlPullParser.get(namespace: String?, key: String): String? = getAttributeValue(namespace, key)
operator fun XmlPullParser.get(key: String): String? = this[null, key]

fun Resources.parseResourceIdentifier(identifier: String, packageName: String): Int {
    return try {
        identifier.substring(1).toInt()
    } catch (e: NumberFormatException) {
        getIdentifier(identifier.substring(1), null, packageName)
    }
}

fun Drawable.isSingleColor(color: Int): Boolean {
    val testColor = posterize(color)
    if (this is ColorDrawable) {
        return posterize(getColor()) == testColor
    }
    val bitmap: Bitmap = Utilities.drawableToBitmap(this) ?: return false
    val height: Int = bitmap.height
    val width: Int = bitmap.width

    val pixels = IntArray(height * width)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val distinctPixels = IntArray(pixels.size) { pixels[it] }

    for (pixel in distinctPixels) {
        if (testColor != posterize(pixel)) {
            return false
        }
    }
    return true
}

/**
 * Creates a new component key from an encoded component key string in the form of
 * [flattenedComponentString#userId].  If the userId is not present, then it defaults
 * to the current user.
 */
fun ComponentKeyKt(context: Context, componentKeyStr: String?): ComponentKey {
    val userDelimiterIndex = componentKeyStr!!.indexOf("#")
    val componentName: ComponentName?
    val user: UserHandle
    if (userDelimiterIndex != -1) {
        val componentStr = componentKeyStr.substring(0, userDelimiterIndex)
        val componentUser = java.lang.Long.valueOf(componentKeyStr.substring(userDelimiterIndex + 1))
        componentName = ComponentName.unflattenFromString(componentStr)
        user = UserManagerCompat.getInstance(context).getUserForSerialNumber(componentUser.toLong())
                ?: Process.myUserHandle()
    } else {
        // No user provided, default to the current user
        componentName = ComponentName.unflattenFromString(componentKeyStr)
        user = Process.myUserHandle()
    }
    return ComponentKey(componentName, user)
}