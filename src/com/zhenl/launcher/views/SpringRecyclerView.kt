package com.zhenl.launcher.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.recyclerview.widget.RecyclerView
import com.zhenl.launcher.getColorAttr

/**
 * Created by lin on 2020/10/23.
 */
open class SpringRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val springManager = SpringEdgeEffect.Manager(this)
    private val scrollBarColor by lazy { context.getColorAttr(android.R.attr.colorControlNormal) }

    open var shouldTranslateSelf = true

    var isTopFadingEdgeEnabled = true

    init {
        edgeEffectFactory = springManager.createFactory()
    }

    override fun draw(canvas: Canvas) {
        springManager.withSpring(canvas, shouldTranslateSelf) {
            super.draw(canvas)
            false
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        springManager.withSpring(canvas, !shouldTranslateSelf) {
            super.dispatchDraw(canvas)
            false
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isTopFadingEdgeEnabled) super.getTopFadingEdgeStrength() else 0f
    }

    /**
     * Called by Android [android.view.View.onDrawScrollBars]
     */
    @Keep
    protected fun onDrawHorizontalScrollBar(canvas: Canvas, scrollBar: Drawable, l: Int, t: Int, r: Int, b: Int) {
        springManager.withSpringNegative(canvas, shouldTranslateSelf) {
            scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
            scrollBar.setBounds(l, t, r, b)
            scrollBar.draw(canvas)
            false
        }
    }

    /**
     * Called by Android [android.view.View.onDrawScrollBars]
     */
    @Keep
    protected fun onDrawVerticalScrollBar(canvas: Canvas, scrollBar: Drawable, l: Int, t: Int, r: Int, b: Int) {
        springManager.withSpringNegative(canvas, shouldTranslateSelf) {
            scrollBar.setColorFilter(scrollBarColor, PorterDuff.Mode.SRC_ATOP)
            scrollBar.setBounds(l, t, r, b)
            scrollBar.draw(canvas)
            false
        }
    }
}