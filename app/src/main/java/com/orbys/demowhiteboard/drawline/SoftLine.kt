package com.orbys.demowhiteboard.drawline

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF

class SoftLine(private val mLineData: LineData) {
    private var mPath: Path? = null
    private val mPaint = Paint()

    fun addPoint(x: Float, y: Float): Data {
        mLineData.config(mPaint)
        mPath = Path()
        var pointF = mLineData.lastPoint()
        if (pointF == null) {
            pointF = PointF(x, y)
        }
        val data = mLineData.addPoint(x, y)
        if (data == null || data.size <= 0) {
            return Data(mPath!!, mPaint)
        }
        val length = data.size
        mPath!!.moveTo(pointF.x, pointF.y)
        var i = 0
        while (i < length) {
            mPath!!.lineTo(data[i], data[i + 1])
            i += 2
        }
        return Data(mPath!!, mPaint)
    }

    class Data(val path: Path, val paint: Paint)
}