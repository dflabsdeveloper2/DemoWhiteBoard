package com.orbys.demowhiteboard.drawline

import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.PointF

class LineData(private val mColor: Int, private val mWidth: Float) {
    private val mTempPoints: MutableList<PointF> = ArrayList()
    private val mPoints: MutableList<PointF> = ArrayList()
    private var mLastPoint: PointF? = null

    fun addPoint(x: Float, y: Float): FloatArray? {
        val rets: FloatArray
        val p = getLastPoint(-1)
        if (p == null) {
            mTempPoints.add(PointF(x, y))
            return null
        }
        val pn = PointF((p.x + x) / 2, (p.y + y) / 2)
        mTempPoints.add(pn)
        mTempPoints.add(PointF(x, y))
        if (mTempPoints.size < 5) {
            rets = floatArrayOf(p.x, p.y, x, y)
        } else {
            val s = getLastPoint(-4)
            val c = getLastPoint(-3)
            val e = getLastPoint(-2)
            val tmpPath = Path()
            tmpPath.moveTo(s!!.x, s.y)
            tmpPath.quadTo(c!!.x, c.y, e!!.x, e.y)
            val measure = PathMeasure(tmpPath, false)
            val len = measure.length
            if (len < sThrd) {
                rets = floatArrayOf(s.x, s.y, e.x, e.y)
            } else {
                val count = (len / 2).toInt()
                rets = FloatArray(count * 2)
                val inc = 1f / count
                var ix = 0
                var j = 0f
                while (j <= 1f) {
                    if (ix >= count * 2) {
                        break
                    }
                    val tx = calc(j, s.x, c.x, e.x)
                    val ty = calc(j, s.y, c.y, e.y)
                    rets[ix] = tx
                    rets[ix + 1] = ty
                    j += inc
                    ix += 2
                }
            }
        }
        var i = 0
        while (i < rets.size) {
            mPoints.add(PointF(rets[i], rets[i + 1]))
            i += 2
        }
        mLastPoint = PointF(mPoints[mPoints.size - 1].x, mPoints[mPoints.size - 1].y)
        return rets
    }

    fun addLastPoint(x: Float, y: Float) {
        mPoints.add(PointF(x, y))
    }

    fun lastPoint(): PointF? {
        return mLastPoint
    }

    fun toPath(): Path {
        val path = Path()
        if (mPoints.size > 1) {
            path.moveTo(mPoints[0].x, mPoints[0].y)
            for (i in 1 until mPoints.size) {
                path.lineTo(mPoints[i].x, mPoints[i].y)
            }
        }
        return path
    }

    val paint: Paint
        get() = config(sPaint)

    fun config(paint: Paint): Paint {
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.color = mColor
        paint.strokeWidth = mWidth
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }

    private fun getLastPoint(dx: Int): PointF? {
        return if (mTempPoints.size < 1) {
            null
        } else mTempPoints[mTempPoints.size + dx]
    }

    companion object {
        const val sThrd = 2f
        var sPaint = Paint()
        private fun calc(t: Float, p0: Float, p1: Float, p2: Float): Float {
            val oneMinusT = 1 - t
            val oneMinusTSquare = oneMinusT * oneMinusT
            val tSquare = t * t
            return oneMinusTSquare * p0 + 2 * oneMinusT * t * p1 + tSquare * p2
        }
    }
}