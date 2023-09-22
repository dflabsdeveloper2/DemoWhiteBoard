package com.orbys.demowhiteboard.eraser

import android.graphics.RectF

class EraseData {
    private val mRegions: MutableList<RectF?> = ArrayList()
    val regions: List<RectF?>
        get() = mRegions

    fun addRegion(rectF: RectF?) {
        mRegions.add(rectF)
    }
}