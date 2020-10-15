package com.zhenl.launcher.adaptive

import android.graphics.Path
import android.graphics.Rect
import android.graphics.Region
import android.graphics.RegionIterator
import android.graphics.drawable.AdaptiveIconDrawable
import com.android.launcher3.Utilities

/**
 * Created by lin on 2020/10/13.
 */
class IconShapeManager {

    private val systemIconShape = getSystemShape()
    var iconShape = systemIconShape

    private fun getSystemShape(): IconShape {
        if (!Utilities.ATLEAST_OREO) return IconShape.Circle

        val iconMask = AdaptiveIconDrawable(null, null).iconMask
        val systemShape = findNearestShape(iconMask)
        return object : IconShape(systemShape) {

            override fun getMaskPath(): Path {
                return Path(iconMask)
            }

            override fun toString() = ""
        }
    }

    private fun findNearestShape(comparePath: Path): IconShape {
        val clip = Region(0, 0, 100, 100)
        val systemRegion = Region().apply {
            setPath(comparePath, clip)
        }
        val pathRegion = Region()
        val path = Path()
        val rect = Rect()
        return listOf(
                IconShape.Circle,
                IconShape.Square,
                IconShape.RoundedSquare,
                IconShape.Squircle,
                IconShape.Teardrop,
                IconShape.Cylinder).minBy {
            path.reset()
            it.addShape(path, 0f, 0f, 50f)
            pathRegion.setPath(path, clip)
            pathRegion.op(systemRegion, Region.Op.XOR)

            var difference = 0
            val iter = RegionIterator(pathRegion)
            while (iter.next(rect)) {
                difference += rect.width() * rect.height()
            }

            difference
        }!!
    }

    companion object {
        val instance by lazy { IconShapeManager() }
    }
}