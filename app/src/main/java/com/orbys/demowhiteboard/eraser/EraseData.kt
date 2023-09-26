package com.orbys.demowhiteboard.eraser

import android.graphics.PointF
import android.graphics.RectF

class EraseData {
    private val mRegions: MutableList<RectF?> = ArrayList()
    val regions: List<RectF?>
        get() = mRegions

    fun addRegion(rectF: RectF?) {
        mRegions.add(rectF)
    }

    fun getListPoints(regions:List<RectF?>):List<PointF>{
        val pointFList = mutableSetOf<PointF>()

        for (rectF in regions) {
            rectF?.let {
                val left = rectF.left.toInt()
                val top = rectF.top.toInt()
                val right = rectF.right.toInt()
                val bottom = rectF.bottom.toInt()

                for (x in left until right) {
                    for (y in top until bottom) {
                        val point = PointF(x.toFloat(), y.toFloat())
                        pointFList.add(point)
                    }
                }
            }
        }

        return pointFList.toMutableList()
    }
}