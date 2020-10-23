package com.zhenl.launcher.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.android.launcher3.Insettable

/**
 * Created by lin on 2020/10/23.
 */
open class PreferenceRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SpringRecyclerView(context, attrs, defStyleAttr), Insettable {

    private val currentInsets = Rect()
    private val currentPadding = Rect()

    override fun setInsets(insets: Rect) {
        super.setPadding(
                paddingLeft + insets.left - currentInsets.left,
                paddingTop + insets.top - currentInsets.top,
                paddingRight + insets.right - currentInsets.right,
                paddingBottom + insets.bottom - currentInsets.bottom)
        currentInsets.set(insets)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(
                paddingLeft + left - currentPadding.left,
                paddingTop + top - currentPadding.top,
                paddingRight + right - currentPadding.right,
                paddingBottom + bottom - currentPadding.bottom)
        currentPadding.set(left, top, right, bottom)
    }
}